import 'package:dio/dio.dart';
import 'package:drift/drift.dart' show driftRuntimeOptions;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/data/database/database.dart';
import 'package:unieats/data/network/api_client.dart';
import 'package:unieats/data/network/app_error.dart';
import 'package:unieats/data/repository/spot_repository.dart';
import 'package:unieats_data/unieats_data.dart';

Spot spot({String name = 'Pizza Stop', int updatedAt = 1000}) => Spot(
  id: 'spot-3',
  name: name,
  category: SpotCategory.fastfood,
  rating: 4.1,
  priceLevel: 2,
  lat: 44.426,
  lng: 26.1015,
  openNow: true,
  photoUrl: '',
  description: 'Pizza by the slice',
  updatedAt: updatedAt,
);

/// Answers PATCH from a queue of scripted results and records each request.
class ScriptedApi extends ApiClient {
  ScriptedApi(this.results) : super(Dio());

  final List<Object> results;
  final patches = <({Map<String, dynamic> body, String key})>[];

  @override
  Future<Spot> patchSpot(
    String id,
    Map<String, dynamic> patch, {
    required String idempotencyKey,
  }) async {
    patches.add((body: patch, key: idempotencyKey));
    final next = results.removeAt(0);
    if (next is Spot) return next;
    throw next;
  }
}

void main() {
  driftRuntimeOptions.dontWarnAboutMultipleDatabases = true;
  const resolver = SyncConflictResolver();

  group('SyncConflictResolver (last-write-wins on updatedAt)', () {
    test('a strictly newer server copy wins', () {
      expect(
        resolver.resolve(
          local: spot(updatedAt: 1000),
          server: spot(updatedAt: 1001),
        ),
        ConflictWinner.server,
      );
    });

    test('a tie keeps the client edit', () {
      expect(
        resolver.resolve(
          local: spot(updatedAt: 1000),
          server: spot(updatedAt: 1000),
        ),
        ConflictWinner.client,
      );
    });

    test('an older server copy loses to the client', () {
      expect(
        resolver.resolve(
          local: spot(updatedAt: 2000),
          server: spot(updatedAt: 1000),
        ),
        ConflictWinner.client,
      );
    });
  });

  group('outbox replay', () {
    late AppDatabase db;

    setUp(() async {
      db = AppDatabase.forTesting(NativeDatabase.memory());
      await db.upsertFromServer([spot()]);
    });
    tearDown(() => db.close());

    Future<void> editOffline(DriftSpotRepository repo) => repo.editSpot(
      'spot-3',
      name: 'Pizza Stop (edited)',
      description: 'Pizza by the slice',
      openNow: false,
    );

    test('2xx stores the server copy and clears pendingSync', () async {
      final api = ScriptedApi([
        spot(name: 'Pizza Stop (edited)', updatedAt: 1500),
      ]);
      final repo = DriftSpotRepository(db, api);
      await editOffline(repo);
      final opId = (await db.pendingOps()).single.opId;

      await repo.syncOutbox();

      expect(api.patches.single.key, opId);
      expect(api.patches.single.body['updatedAt'], 1000);
      expect(await db.pendingOps(), isEmpty);
      final row = await db.getSpot('spot-3');
      expect(row!.pendingSync, isFalse);
      expect(row.updatedAt, 1500);
    });

    test('409 with a newer server copy: server wins, op dropped', () async {
      final api = ScriptedApi([
        Conflict(spot(name: 'Changed by someone else', updatedAt: 1200)),
      ]);
      final repo = DriftSpotRepository(db, api);
      await editOffline(repo);

      await repo.syncOutbox();

      final row = await db.getSpot('spot-3');
      expect(row!.name, 'Changed by someone else');
      expect(row.pendingSync, isFalse);
      expect(await db.pendingOps(), isEmpty);
    });

    test(
      '409 with a tied updatedAt: client wins and the edit is resent',
      () async {
        final api = ScriptedApi([
          Conflict(spot(name: 'Server copy', updatedAt: 1000)),
          spot(name: 'Pizza Stop (edited)', updatedAt: 1300),
        ]);
        final repo = DriftSpotRepository(db, api);
        await editOffline(repo);

        await repo.syncOutbox();

        expect(api.patches, hasLength(2));
        expect(api.patches.last.key, isNot(api.patches.first.key));
        final row = await db.getSpot('spot-3');
        expect(row!.name, 'Pizza Stop (edited)');
        expect(row.pendingSync, isFalse);
        expect(await db.pendingOps(), isEmpty);
      },
    );

    test('offline: the op and the local edit stay for the next try', () async {
      final api = ScriptedApi([const NoConnectivity()]);
      final repo = DriftSpotRepository(db, api);
      await editOffline(repo);

      await repo.syncOutbox();

      expect(await db.pendingOps(), hasLength(1));
      final row = await db.getSpot('spot-3');
      expect(row!.name, 'Pizza Stop (edited)');
      expect(row.pendingSync, isTrue);
    });

    test('401 keeps the op until the user signs in again', () async {
      final api = ScriptedApi([const HttpError(401, 'unauthorized')]);
      final repo = DriftSpotRepository(db, api);
      await editOffline(repo);

      await repo.syncOutbox();

      expect(await db.pendingOps(), hasLength(1));
    });
  });
}
