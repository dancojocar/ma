import 'dart:async';

import 'package:drift/drift.dart' show driftRuntimeOptions;
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:unieats/data/database/database.dart';
import 'package:unieats/data/network/app_error.dart';
import 'package:unieats/data/repository/review_repository.dart';
import 'package:unieats/data/repository/spot_repository.dart';
import 'package:unieats_data/unieats_data.dart';

import 'support/fakes.dart';

void main() {
  driftRuntimeOptions.dontWarnAboutMultipleDatabases = true;

  late AppDatabase db;
  late ScriptedApi api;
  late DriftSpotRepository spots;
  late DriftReviewRepository reviews;

  setUp(() async {
    db = AppDatabase.forTesting(NativeDatabase.memory());
    api = ScriptedApi();
    spots = DriftSpotRepository(db, api);
    reviews = DriftReviewRepository(db, api);
    await db.upsertFromServer([
      spot('spot-1', name: 'Espresso Lab'),
      spot('spot-2', name: 'Green Bowl'),
      spot('spot-3', name: 'Pizza Stop'),
    ]);
  });
  tearDown(() => db.close());

  Future<void> edit(String id, String name, {User user = alice}) =>
      spots.editSpot(
        id,
        name: name,
        description: '',
        openNow: false,
        userId: user.id,
      );

  Future<SpotRow> row(String id) async => (await db.getSpot(id))!;

  test('2xx stores the server copy and clears pendingSync', () async {
    await edit('spot-3', 'Pizza Stop (edited)');
    final opId = (await db.pendingOps()).single.opId;
    api.patchResults.add(
      spot('spot-3', name: 'Pizza Stop (edited)', updatedAt: 1500),
    );

    await spots.syncOutbox();

    expect(api.patches.single.key, opId, reason: 'opId is the Idempotency-Key');
    expect(api.patches.single.body, {
      'name': 'Pizza Stop (edited)',
      'description': '',
      'openNow': false,
      'updatedAt': 1000,
    });
    expect(await db.pendingOps(), isEmpty);
    expect((await row('spot-3')).pendingSync, isFalse);
    expect((await row('spot-3')).updatedAt, 1500);
  });

  test('ops replay in the order they were made, reviews included', () async {
    await edit('spot-2', 'B');
    await reviews.addReview('spot-1', stars: 4, text: 'Nice', author: alice);
    await edit('spot-3', 'C');
    await edit('spot-2', 'B2');
    api.patchResults.addAll([
      spot('spot-2', name: 'B', updatedAt: 1100),
      spot('spot-3', name: 'C', updatedAt: 1100),
      spot('spot-2', name: 'B2', updatedAt: 1200),
    ]);
    api.reviewResults.add(review('review-9', spotId: 'spot-1'));

    await spots.syncOutbox();

    expect(api.calls, [
      'PATCH spot-2',
      'POST spot-1',
      'PATCH spot-3',
      'PATCH spot-2',
    ]);
    expect(
      api.patches.last.body['updatedAt'],
      1100,
      reason: 'the second edit is based on the version the first one saved',
    );
    expect(await db.pendingOps(), isEmpty);
    expect((await row('spot-2')).name, 'B2');
    expect((await db.watchReviews('spot-1').first).map((r) => r.id), [
      'review-9',
    ], reason: 'the local: review is swapped for the server copy');
  });

  test('a replay already running is joined, not started twice', () async {
    await edit('spot-3', 'C');
    final gate = Completer<Spot>();
    api.patchResults.add(gate.future);

    final first = spots.syncOutbox();
    final second = spots.syncOutbox();
    gate.complete(spot('spot-3', name: 'C', updatedAt: 1100));
    await Future.wait([first, second]);

    expect(api.patches, hasLength(1));
  });

  group('an edit made while its PATCH is in flight', () {
    late Completer<Spot> inFlight;
    late Future<void> replay;

    setUp(() async {
      await edit('spot-3', 'A');
      inFlight = Completer<Spot>();
      api.patchResults.add(inFlight.future);
      replay = spots.syncOutbox();
      await pumpEventQueue();
      expect(api.patches, hasLength(1), reason: 'the PATCH for A is in flight');
      await edit('spot-3', 'B');
    });

    test('survives and is sent next', () async {
      api.patchResults.add(spot('spot-3', name: 'B', updatedAt: 1400));
      inFlight.complete(spot('spot-3', name: 'A', updatedAt: 1300));
      await replay;

      expect(api.patches.map((p) => p.body['name']), ['A', 'B']);
      expect(api.patches.last.body['updatedAt'], 1300);
      expect((await row('spot-3')).name, 'B');
      expect((await row('spot-3')).pendingSync, isFalse);
      expect(await db.pendingOps(), isEmpty);
    });

    test(
      'stays pending on the new base version if the network drops',
      () async {
        inFlight.complete(spot('spot-3', name: 'A', updatedAt: 1300));
        await replay;

        final r = await row('spot-3');
        expect(r.name, 'B');
        expect(r.pendingSync, isTrue);
        expect(r.updatedAt, 1300);
        expect((await db.pendingOps()).single.entityId, 'spot-3');
      },
    );
  });

  group('409: the server always wins', () {
    test('the server copy replaces the row and the op is dropped', () async {
      await edit('spot-3', 'Mine');
      api.patchResults.add(
        Conflict(
          spot('spot-3', name: 'Changed by someone else', updatedAt: 1200),
        ),
      );

      await spots.syncOutbox();

      final r = await row('spot-3');
      expect(r.name, 'Changed by someone else');
      expect(r.updatedAt, 1200);
      expect(r.pendingSync, isFalse);
      expect(await db.pendingOps(), isEmpty);
    });

    test('later edits of that spot go too; other spots still replay', () async {
      await edit('spot-3', 'Mine 1');
      await edit('spot-1', 'Other');
      await edit('spot-3', 'Mine 2');
      api.patchResults.addAll([
        Conflict(spot('spot-3', name: 'Server', updatedAt: 1200)),
        spot('spot-1', name: 'Other', updatedAt: 1100),
      ]);

      await spots.syncOutbox();

      expect(api.calls, ['PATCH spot-3', 'PATCH spot-1']);
      expect((await row('spot-3')).name, 'Server');
      expect((await row('spot-3')).pendingSync, isFalse);
      expect((await row('spot-1')).name, 'Other');
      expect(await db.pendingOps(), isEmpty);
    });
  });

  group('permanent 4xx', () {
    test('drops the op and shows the server copy', () async {
      await edit('spot-3', 'Rejected');
      api.patchResults.add(const HttpError(422, 'name too long'));
      api.fetchSpotResults.add(
        spot('spot-3', name: 'Pizza Stop', updatedAt: 1100),
      );

      await spots.syncOutbox();

      expect(api.calls, ['PATCH spot-3', 'GET spot-3']);
      expect((await row('spot-3')).name, 'Pizza Stop');
      expect((await row('spot-3')).pendingSync, isFalse);
      expect(await db.pendingOps(), isEmpty);
    });

    test('keeps a later queued edit of the same spot and sends it', () async {
      await edit('spot-3', 'Rejected');
      await edit('spot-3', 'Accepted');
      api.patchResults.addAll([
        const HttpError(400, 'bad'),
        spot('spot-3', name: 'Accepted', updatedAt: 1200),
      ]);
      api.fetchSpotResults.add(spot('spot-3', updatedAt: 1100));

      await spots.syncOutbox();

      expect(api.calls, ['PATCH spot-3', 'GET spot-3', 'PATCH spot-3']);
      expect(api.patches.last.body['updatedAt'], 1100);
      expect((await row('spot-3')).name, 'Accepted');
      expect(await db.pendingOps(), isEmpty);
    });

    test('keeps the op when the server copy cannot be fetched', () async {
      await edit('spot-3', 'Rejected');
      api.patchResults.add(const HttpError(422));

      await spots.syncOutbox();

      expect(await db.pendingOps(), hasLength(1));
      expect((await row('spot-3')).name, 'Rejected');
    });

    test('404 deletes the spot and its ops', () async {
      await edit('spot-3', 'Gone');
      api.patchResults.add(const HttpError(404));

      await spots.syncOutbox();

      expect(await db.getSpot('spot-3'), isNull);
      expect(await db.pendingOps(), isEmpty);
    });

    test('a rejected review is removed', () async {
      await reviews.addReview('spot-1', stars: 1, text: '', author: alice);
      api.reviewResults.add(const HttpError(400, 'stars'));

      await spots.syncOutbox();

      expect(await db.watchReviews('spot-1').first, isEmpty);
      expect(await db.pendingOps(), isEmpty);
    });
  });

  group('not permanent: the op stays and the replay stops', () {
    for (final (name, error) in <(String, AppError)>[
      ('offline', const NoConnectivity()),
      ('timeout', const TimeoutError()),
      ('401 until the user signs in again', const HttpError(401)),
      ('429', const HttpError(429)),
      ('500', const HttpError(500)),
    ]) {
      test(name, () async {
        await edit('spot-3', 'Mine');
        await edit('spot-1', 'Later');
        api.patchResults.add(error);

        await spots.syncOutbox();

        expect(api.calls, ['PATCH spot-3']);
        expect(await db.pendingOps(), hasLength(2));
        expect((await row('spot-3')).name, 'Mine');
        expect((await row('spot-3')).pendingSync, isTrue);
      });
    }

    test('a review retried later reuses its Idempotency-Key', () async {
      await reviews.addReview('spot-1', stars: 5, text: 'Yum', author: alice);

      await spots.syncOutbox();
      api.reviewResults.add(review('review-1', spotId: 'spot-1'));
      await spots.syncOutbox();

      expect(api.reviewKeys, hasLength(2));
      expect(api.reviewKeys.toSet(), hasLength(1));
      expect((await db.watchReviews('spot-1').first).single.id, 'review-1');
    });
  });

  group('live events', () {
    test('spot.deleted removes the row and its queued ops', () async {
      await edit('spot-3', 'Mine');

      await spots.applyLiveEvent(const SpotDeleted('spot-3'));

      expect(await db.getSpot('spot-3'), isNull);
      expect(await db.pendingOps(), isEmpty);
    });

    test('spot.updated never overwrites an unsent edit', () async {
      await edit('spot-3', 'Mine');

      await spots.applyLiveEvent(
        SpotChanged(spot('spot-3', name: 'Server', updatedAt: 2000)),
      );

      expect((await row('spot-3')).name, 'Mine');
    });
  });
}
