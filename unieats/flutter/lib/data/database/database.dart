import 'package:drift/drift.dart';
import 'package:drift_flutter/drift_flutter.dart';

import '../../domain/models.dart';
import 'tables.dart';

part 'database.g.dart';

@DriftDatabase(tables: [Spots, Reviews, Outbox])
class AppDatabase extends _$AppDatabase {
  AppDatabase() : super(driftDatabase(name: 'unieats'));

  AppDatabase.forTesting(super.executor);

  @override
  int get schemaVersion => 1;

  Stream<List<Spot>> watchSpots({String query = '', SpotCategory? category}) {
    final pattern = '%${query.trim()}%';
    final select =
        this.select(spots)
          ..where(
            (t) =>
                (t.name.like(pattern) | t.description.like(pattern)) &
                (category == null
                    ? const Constant(true)
                    : t.category.equals(category.name)),
          )
          ..orderBy([(t) => OrderingTerm.asc(t.name.collate(Collate.noCase))]);
    return select.watch().map((rows) => rows.map((r) => r.toSpot()).toList());
  }

  Stream<Spot?> watchSpot(String id) => (select(
    spots,
  )..where((t) => t.id.equals(id))).watchSingleOrNull().map((r) => r?.toSpot());

  Future<SpotRow?> getSpot(String id) =>
      (select(spots)..where((t) => t.id.equals(id))).getSingleOrNull();

  /// Upsert by id; rows with an unsynced local edit are left untouched.
  Future<void> upsertFromServer(List<Spot> serverSpots) => batch((b) {
    for (final spot in serverSpots) {
      b.insert(
        spots,
        spot.toCompanion(),
        onConflict: DoUpdate<$SpotsTable, SpotRow>(
          (_) => spot.toCompanion(),
          where: (old) => old.pendingSync.equals(false),
        ),
      );
    }
  });

  Future<void> deleteSpot(String id) =>
      (delete(spots)..where((t) => t.id.equals(id))).go();

  Future<void> applyLocalEdit(
    String id, {
    required String name,
    required String description,
    required bool openNow,
  }) => (update(spots)..where((t) => t.id.equals(id))).write(
    SpotsCompanion(
      name: Value(name),
      description: Value(description),
      openNow: Value(openNow),
      pendingSync: const Value(true),
    ),
  );

  Future<List<SpotRow>> getPendingSync() =>
      (select(spots)..where((t) => t.pendingSync.equals(true))).get();

  /// Stores the server's version of a spot and clears its pending flag.
  Future<void> markSynced(Spot serverSpot) =>
      into(spots).insertOnConflictUpdate(serverSpot.toCompanion());

  /// Moves the base version forward after one of several queued edits synced.
  Future<void> setBaseVersion(String id, int updatedAt) => (update(spots)
    ..where(
      (t) => t.id.equals(id),
    )).write(SpotsCompanion(updatedAt: Value(updatedAt)));

  Stream<List<Review>> watchReviews(String spotId) => (select(reviews)
        ..where((t) => t.spotId.equals(spotId))
        ..orderBy([(t) => OrderingTerm.desc(t.createdAt)]))
      .watch()
      .map((rows) => rows.map((r) => r.toReview()).toList());

  /// Makes the local reviews of [spotId] match [serverReviews] in one transaction.
  Future<void> replaceReviews(String spotId, List<Review> serverReviews) =>
      transaction(() async {
        final ids = serverReviews.map((r) => r.id).toList();
        await (delete(reviews)
          ..where((t) => t.spotId.equals(spotId) & t.id.isNotIn(ids))).go();
        await batch((b) {
          for (final review in serverReviews) {
            b.insert(
              reviews,
              review.toCompanion(),
              mode: InsertMode.insertOrReplace,
            );
          }
        });
      });

  Future<void> enqueue({
    required String opId,
    required String type,
    required String entityId,
    required String payload,
  }) => into(outbox).insert(
    OutboxCompanion.insert(
      opId: opId,
      type: type,
      entityId: entityId,
      payload: payload,
      createdAt: DateTime.now().millisecondsSinceEpoch,
    ),
  );

  Future<List<OutboxOp>> pendingOps() =>
      (select(outbox)..orderBy([(t) => OrderingTerm.asc(t.seq)])).get();

  Future<bool> hasPendingOps(String entityId) async =>
      await (select(outbox)
            ..where((t) => t.entityId.equals(entityId))
            ..limit(1))
          .getSingleOrNull() !=
      null;

  Future<void> removeOp(String opId) =>
      (delete(outbox)..where((t) => t.opId.equals(opId))).go();

  Future<void> removeOpsFor(String entityId) =>
      (delete(outbox)..where((t) => t.entityId.equals(entityId))).go();

  Stream<int> watchOutboxCount() =>
      select(outbox).watch().map((ops) => ops.length);

  Stream<Set<String>> watchPendingEntityIds() =>
      select(outbox).watch().map((ops) => {for (final op in ops) op.entityId});
}

extension SpotRowMapping on SpotRow {
  Spot toSpot() => Spot(
    id: id,
    name: name,
    category: SpotCategory.values.byName(category),
    rating: rating,
    priceLevel: priceLevel,
    lat: lat,
    lng: lng,
    openNow: openNow,
    photoUrl: photoUrl,
    description: description,
    updatedAt: updatedAt,
  );
}

extension SpotMapping on Spot {
  SpotsCompanion toCompanion() => SpotsCompanion.insert(
    id: id,
    name: name,
    category: category.name,
    rating: rating,
    priceLevel: priceLevel,
    lat: lat,
    lng: lng,
    openNow: openNow,
    photoUrl: photoUrl,
    description: description,
    updatedAt: updatedAt,
    pendingSync: const Value(false),
  );
}

extension ReviewRowMapping on ReviewRow {
  Review toReview() => Review(
    id: id,
    spotId: spotId,
    author: author,
    stars: stars,
    text: body,
    createdAt: createdAt,
  );
}

extension ReviewMapping on Review {
  ReviewsCompanion toCompanion() => ReviewsCompanion.insert(
    id: id,
    spotId: spotId,
    author: author,
    stars: stars,
    body: text,
    createdAt: createdAt,
  );
}
