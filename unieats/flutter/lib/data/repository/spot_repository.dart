import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:uuid/uuid.dart';

import '../../domain/models.dart';
import '../database/database.dart';
import '../network/api_client.dart';
import '../network/app_error.dart';
import '../network/live_updates.dart';
import '../sync/sync_conflict_resolver.dart';

abstract final class OutboxType {
  static const updateSpot = 'update';

  /// Not `create`: that type is reserved for creating spots (CONTRACT §3).
  static const createReview = 'createReview';
}

/// The UI only reads the local database; this class keeps it in step with the
/// server and owns the outbox of local edits.
class SpotRepository {
  SpotRepository(
    this._db,
    this._api, {
    SyncConflictResolver resolver = const SyncConflictResolver(),
  }) : _resolver = resolver;

  final AppDatabase _db;
  final ApiClient _api;
  final SyncConflictResolver _resolver;
  final _uuid = const Uuid();
  Future<void>? _syncInFlight;

  Stream<List<Spot>> watchSpots({String query = '', SpotCategory? category}) =>
      _db.watchSpots(query: query, category: category);

  Stream<Spot?> watchSpot(String id) => _db.watchSpot(id);

  Stream<Set<String>> watchPendingIds() => _db.watchPendingEntityIds();

  Stream<int> watchPendingCount() => _db.watchOutboxCount();

  Future<SpotsPage> refreshPage({
    required int page,
    required int limit,
    String query = '',
    SpotCategory? category,
    CancelToken? cancelToken,
  }) async {
    final result = await _api.fetchSpots(
      page: page,
      limit: limit,
      query: query,
      category: category?.name,
      cancelToken: cancelToken,
    );
    await _db.upsertFromServer(result.spots);
    return result;
  }

  Future<void> refreshSpot(String id) async {
    await _db.upsertFromServer([await _api.fetchSpot(id)]);
  }

  Future<void> applyLiveEvent(LiveEvent event) async {
    switch (event) {
      case SpotChanged(:final spot):
        await _db.upsertFromServer([spot]);
      case SpotDeleted(:final id):
        await _db.transaction(() async {
          await _db.removeOpsFor(id);
          await _db.deleteSpot(id);
        });
      case LiveConnected() || LiveDisconnected():
        break;
    }
  }

  /// Optimistic edit: the row changes and is marked `pendingSync` at once, and
  /// an `update` op carrying the edited version's `updatedAt` joins the outbox.
  Future<void> editSpot(
    String id, {
    required String name,
    required String description,
    required bool openNow,
  }) => _db.transaction(() async {
    final row = await _db.getSpot(id);
    if (row == null) throw StateError('Spot $id is not stored locally');
    await _db.applyLocalEdit(
      id,
      name: name,
      description: description,
      openNow: openNow,
    );
    await _db.enqueue(
      opId: _uuid.v4(),
      type: OutboxType.updateSpot,
      entityId: id,
      payload: jsonEncode({
        'name': name,
        'description': description,
        'openNow': openNow,
        'updatedAt': row.updatedAt,
      }),
    );
  });

  /// Replays the outbox in order. Stops at the first network failure and
  /// leaves the remaining ops for the next trigger (reconnect, app start).
  Future<void> syncOutbox() =>
      _syncInFlight ??= _replayAll().whenComplete(() => _syncInFlight = null);

  Future<void> _replayAll() async {
    while (true) {
      final ops = await _db.pendingOps();
      if (ops.isEmpty || !await _replay(ops.first)) return;
    }
  }

  Future<bool> _replay(OutboxOp op) async {
    switch (op.type) {
      case OutboxType.updateSpot:
        return _replayUpdate(op);
      case OutboxType.createReview:
        return _replayReview(op);
      default:
        await _db.removeOp(op.opId);
        return true;
    }
  }

  Future<bool> _replayReview(OutboxOp op) async {
    final payload = jsonDecode(op.payload) as Map<String, dynamic>;
    try {
      final saved = await _api.createReview(
        payload['spotId'] as String,
        stars: payload['stars'] as int,
        text: payload['text'] as String,
        idempotencyKey: op.opId,
      );
      await _db.transaction(() async {
        await _db.removeOp(op.opId);
        await _db.confirmReview(op.entityId, saved);
      });
    } on HttpError catch (e) {
      if (!_isPermanent(e.statusCode)) return false;
      await _db.transaction(() async {
        await _db.removeOp(op.opId);
        await _db.deleteReview(op.entityId);
      });
    } on AppError {
      return false;
    }
    return true;
  }

  Future<bool> _replayUpdate(OutboxOp op) async {
    final id = op.entityId;
    final row = await _db.getSpot(id);
    if (row == null) {
      await _db.removeOp(op.opId);
      return true;
    }
    final patch = {
      ...jsonDecode(op.payload) as Map<String, dynamic>,
      // An earlier op in this replay may have moved the base version forward.
      'updatedAt': row.updatedAt,
    };
    try {
      final saved = await _api.patchSpot(id, patch, idempotencyKey: op.opId);
      await _db.transaction(() async {
        await _db.removeOp(op.opId);
        if (await _db.hasPendingOps(id)) {
          await _db.setBaseVersion(id, saved.updatedAt);
        } else {
          await _db.markSynced(saved);
        }
      });
    } on Conflict catch (conflict) {
      await _resolveConflict(op, row.toSpot(), conflict.serverSpot);
    } on HttpError catch (e) {
      if (e.statusCode == 404) {
        await _db.transaction(() async {
          await _db.removeOpsFor(id);
          await _db.deleteSpot(id);
        });
      } else if (_isPermanent(e.statusCode)) {
        // The server will never accept this edit: drop it, show the server copy.
        final Spot server;
        try {
          server = await _api.fetchSpot(id);
        } on AppError {
          return false;
        }
        await _db.transaction(() async {
          await _db.removeOpsFor(id);
          await _db.markSynced(server);
        });
      } else {
        return false;
      }
    } on AppError {
      return false;
    }
    return true;
  }

  /// 401 is not permanent: the op waits until the user signs in again.
  bool _isPermanent(int status) =>
      status >= 400 &&
      status < 500 &&
      status != 401 &&
      status != 408 &&
      status != 429;

  Future<void> _resolveConflict(OutboxOp op, Spot local, Spot server) =>
      _db.transaction(() async {
        switch (_resolver.resolve(local: local, server: server)) {
          case ConflictWinner.server:
            await _db.removeOpsFor(local.id);
            await _db.markSynced(server);
          case ConflictWinner.client:
            await _db.removeOp(op.opId);
            await _db.setBaseVersion(local.id, server.updatedAt);
            await _db.enqueue(
              opId: _uuid.v4(),
              type: op.type,
              entityId: op.entityId,
              payload: op.payload,
            );
        }
      });
}
