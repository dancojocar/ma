import 'package:drift/drift.dart' show driftRuntimeOptions;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/data/database/database.dart';
import 'package:unieats_data/unieats_data.dart';

import 'support/fakes.dart';

void main() {
  // Every test opens its own in-memory database, so several instances are fine.
  driftRuntimeOptions.dontWarnAboutMultipleDatabases = true;
  late AppDatabase db;

  setUp(() => db = AppDatabase.forTesting(NativeDatabase.memory()));
  tearDown(() => db.close());

  group('upsertFromServer', () {
    test('inserts new rows and updates existing ones by id', () async {
      await db.upsertFromServer([spot('spot-1', name: 'Old')]);
      await db.upsertFromServer([spot('spot-1', name: 'New'), spot('spot-2')]);

      final spots = await db.watchSpots().first;

      expect(spots.map((s) => s.id), containsAll(['spot-1', 'spot-2']));
      expect((await db.getSpot('spot-1'))!.name, 'New');
    });

    test('never overwrites a row with an unsynced local edit', () async {
      await db.upsertFromServer([spot('spot-1', name: 'Server')]);
      await db.applyLocalEdit(
        'spot-1',
        name: 'Mine',
        description: 'edited offline',
        openNow: false,
      );

      await db.upsertFromServer([spot('spot-1', name: 'Server v2')]);

      final row = await db.getSpot('spot-1');
      expect(row!.name, 'Mine');
      expect(row.pendingSync, isTrue);
    });
  });

  test('getPendingSync / markSynced track local edits', () async {
    await db.upsertFromServer([spot('spot-1'), spot('spot-2')]);
    await db.applyLocalEdit(
      'spot-2',
      name: 'Edited',
      description: '',
      openNow: true,
    );

    expect((await db.getPendingSync()).map((r) => r.id), ['spot-2']);

    await db.markSynced(spot('spot-2', name: 'Edited', updatedAt: 2000));

    expect(await db.getPendingSync(), isEmpty);
    expect((await db.getSpot('spot-2'))!.updatedAt, 2000);
  });

  test('watchSpots filters by query and category, sorted by name', () async {
    await db.upsertFromServer([
      spot('spot-3', name: 'Pizza Stop', category: SpotCategory.fastfood),
      spot(
        'spot-6',
        name: 'Sushi Box',
        category: SpotCategory.fastfood,
        description: 'pizza on fridays',
      ),
      spot('spot-2', name: 'Espresso Lab'),
    ]);

    final pizza = await db.watchSpots(query: 'PIZZA').first;
    final fastfood = await db.watchSpots(category: SpotCategory.fastfood).first;
    final all = await db.watchSpots().first;

    expect(pizza.map((s) => s.name), ['Pizza Stop', 'Sushi Box']);
    expect(fastfood.length, 2);
    expect(all.map((s) => s.name), ['Espresso Lab', 'Pizza Stop', 'Sushi Box']);
  });

  test(
    'the outbox keeps insertion order and survives partial removal',
    () async {
      for (final opId in ['a', 'b', 'c']) {
        await db.enqueue(
          opId: opId,
          type: 'update',
          entityId: opId == 'b' ? 'spot-2' : 'spot-1',
          payload: '{}',
          userId: alice.id,
        );
      }

      expect((await db.pendingOps()).map((op) => op.opId), ['a', 'b', 'c']);

      await db.removeOpsFor('spot-1');

      expect((await db.pendingOps()).map((op) => op.opId), ['b']);
      expect(await db.hasPendingOps('spot-2'), isTrue);
    },
  );

  test('replaceReviews mirrors the server but keeps local reviews', () async {
    await db.insertReview(review('stale'));
    await db.insertReview(review('${localIdPrefix}x'));

    await db.replaceReviews('spot-1', [review('review-1')]);

    final ids = (await db.watchReviews('spot-1').first).map((r) => r.id);
    expect(ids, unorderedEquals(['review-1', '${localIdPrefix}x']));
  });

  group('discarding local changes', () {
    Future<void> queueEdit(String id, String userId) async {
      await db.applyLocalEdit(id, name: 'Mine', description: '', openNow: true);
      await db.enqueue(
        opId: '$id-$userId',
        type: 'update',
        entityId: id,
        payload: '{}',
        userId: userId,
      );
    }

    setUp(() async {
      await db.upsertFromServer([spot('spot-1'), spot('spot-2')]);
      await queueEdit('spot-1', alice.id);
      await db.insertReview(review('${localIdPrefix}r', spotId: 'spot-2'));
      await db.enqueue(
        opId: 'review',
        type: 'createReview',
        entityId: '${localIdPrefix}r',
        payload: '{}',
        userId: alice.id,
      );
      await queueEdit('spot-2', bob.id);
    });

    test('sign-out drops every op, local review and pending row', () async {
      await db.discardLocalChanges();

      expect(await db.pendingOps(), isEmpty);
      expect(await db.getPendingSync(), isEmpty);
      expect(await db.watchReviews('spot-2').first, isEmpty);
    });

    test('another user signing in drops only the other user\'s ops', () async {
      await db.discardOpsNotBy(bob.id);

      expect((await db.pendingOps()).map((op) => op.opId), ['spot-2-u-bob']);
      expect(await db.getSpot('spot-1'), isNull);
      expect((await db.getSpot('spot-2'))!.name, 'Mine');
      expect(await db.watchReviews('spot-2').first, isEmpty);
    });
  });

  test('schema v1 → v2 renames reviews.body to text and keeps the outbox, '
      'with no user on old ops', () async {
    await db.close();
    db = AppDatabase.forTesting(
      NativeDatabase.memory(
        setup: (raw) => raw.execute('''
CREATE TABLE spots (id TEXT NOT NULL, name TEXT NOT NULL, category TEXT NOT NULL,
  rating REAL NOT NULL, price_level INTEGER NOT NULL, lat REAL NOT NULL,
  lng REAL NOT NULL, open_now INTEGER NOT NULL CHECK ("open_now" IN (0, 1)),
  photo_url TEXT NOT NULL, description TEXT NOT NULL, updated_at INTEGER NOT NULL,
  pending_sync INTEGER NOT NULL DEFAULT 0 CHECK ("pending_sync" IN (0, 1)),
  PRIMARY KEY (id));
CREATE TABLE reviews (id TEXT NOT NULL, spot_id TEXT NOT NULL, author TEXT NOT NULL,
  stars INTEGER NOT NULL, body TEXT NOT NULL, created_at INTEGER NOT NULL,
  PRIMARY KEY (id));
CREATE TABLE outbox (seq INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  op_id TEXT NOT NULL UNIQUE, type TEXT NOT NULL, entity_id TEXT NOT NULL,
  payload TEXT NOT NULL, created_at INTEGER NOT NULL);
INSERT INTO spots VALUES ('spot-1', 'Mine', 'cafe', 4, 1, 0, 0, 1, '', '', 1000, 1);
INSERT INTO reviews VALUES ('r1', 'spot-1', 'Ana', 4, 'Hot soup', 1);
INSERT INTO outbox (op_id, type, entity_id, payload, created_at)
  VALUES ('legacy', 'update', 'spot-1', '{}', 0);
PRAGMA user_version = 1;
'''),
      ),
    );

    expect((await db.watchReviews('spot-1').first).single.text, 'Hot soup');
    expect((await db.pendingOps()).single.userId, isNull);
    expect((await db.getSpot('spot-1'))!.pendingSync, isTrue);

    await db.discardOpsNotBy(alice.id);

    expect(
      await db.pendingOps(),
      hasLength(1),
      reason: 'an op from before accounts belongs to no other user',
    );
  });
}
