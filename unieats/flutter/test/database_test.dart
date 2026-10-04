import 'package:drift/drift.dart' show driftRuntimeOptions;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/data/database/database.dart';
import 'package:unieats_data/unieats_data.dart';

Spot spot(
  String id, {
  String name = 'Spot',
  SpotCategory category = SpotCategory.cafe,
  String description = '',
  int updatedAt = 1000,
}) => Spot(
  id: id,
  name: name,
  category: category,
  rating: 4,
  priceLevel: 1,
  lat: 44.427,
  lng: 26.103,
  openNow: true,
  photoUrl: '',
  description: description,
  updatedAt: updatedAt,
);

Review review(String id, {String spotId = 'spot-1'}) => Review(
  id: id,
  spotId: spotId,
  author: 'Ana',
  stars: 5,
  text: 'Good',
  createdAt: 1,
);

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

  test('schema v1 → v2 renames reviews.body to text, keeping data', () async {
    await db.close();
    db = AppDatabase.forTesting(
      NativeDatabase.memory(
        setup:
            (raw) =>
                raw
                  ..execute(
                    'CREATE TABLE reviews (id TEXT NOT NULL PRIMARY KEY, '
                    'spot_id TEXT NOT NULL, author TEXT NOT NULL, '
                    'stars INTEGER NOT NULL, body TEXT NOT NULL, '
                    'created_at INTEGER NOT NULL)',
                  )
                  ..execute(
                    "INSERT INTO reviews VALUES ('r1', 'spot-1', 'Ana', 4, 'Hot soup', 1)",
                  )
                  ..execute('PRAGMA user_version = 1'),
      ),
    );

    final reviews = await db.watchReviews('spot-1').first;

    expect(reviews.single.text, 'Hot soup');
  });
}
