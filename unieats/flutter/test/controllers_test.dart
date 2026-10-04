import 'dart:async';

import 'package:drift/drift.dart' show driftRuntimeOptions;
import 'package:drift/native.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:local_auth/local_auth.dart';
import 'package:unieats/data/auth/token_store.dart';
import 'package:unieats/data/database/database.dart';
import 'package:unieats/data/network/app_error.dart';
import 'package:unieats/providers/providers.dart';
import 'package:unieats_data/unieats_data.dart';

import 'support/fakes.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  driftRuntimeOptions.dontWarnAboutMultipleDatabases = true;

  late Map<String, String> plain;
  late FakeBiometricStorage biometric;
  late ScriptedApi api;
  late AppDatabase db;

  setUp(() async {
    plain = {};
    FlutterSecureStorage.setMockInitialValues(plain);
    biometric = FakeBiometricStorage();
    api = ScriptedApi();
    db = AppDatabase.forTesting(NativeDatabase.memory());
    await db.upsertFromServer([spot('s1'), spot('s2'), spot('s3')]);
  });
  tearDown(() => db.close());

  /// A new container is a new app start over the same device storage.
  ProviderContainer start({
    List<BiometricType> biometrics = const [BiometricType.strong],
  }) => ProviderContainer.test(
    retry: (_, _) => null,
    overrides: [
      databaseProvider.overrideWithValue(db),
      apiClientProvider.overrideWithValue(api),
      tokenStoreProvider.overrideWithValue(TokenStore(null, biometric)),
      localAuthProvider.overrideWithValue(FakeLocalAuth(biometrics)),
    ],
  );

  Future<int> outboxCount() async => (await db.pendingOps()).length;

  Future<ProviderContainer> signedIn([User user = alice]) async {
    api.nextUser = user;
    final c = start();
    await c.read(authProvider.future);
    await c.read(authProvider.notifier).login('${user.id}@x', 'password');
    return c;
  }

  group('AuthNotifier', () {
    test(
      'starts SignedOut; login -> SignedIn and replays the outbox',
      () async {
        await db.enqueue(
          opId: 'queued',
          type: 'update',
          entityId: 's1',
          payload: '{"name":"x","description":"","openNow":true,"updatedAt":1}',
          userId: alice.id,
        );
        final c = start();
        expect(await c.read(authProvider.future), const SignedOut());

        await c.read(authProvider.notifier).login('a@x', 'password');
        await pumpEventQueue();

        expect(c.read(authProvider).value, const SignedIn(alice));
        expect(c.read(signedInUserProvider), alice);
        expect(api.calls, ['PATCH s1']);
      },
    );

    test('a failed login is an error state, still signed out', () async {
      api.loginError = const HttpError(401, 'Invalid email or password');
      final c = start();
      await c.read(authProvider.future);

      await c.read(authProvider.notifier).login('a@x', 'wrong');

      expect(c.read(authProvider).error, isA<HttpError>());
      expect(c.read(signedInUserProvider), isNull);
    });

    test('a 401 ends the session with SessionExpired and keeps the '
        'outbox', () async {
      final c = await signedIn();
      await c
          .read(spotRepositoryProvider)
          .editSpot(
            's1',
            name: 'x',
            description: '',
            openNow: true,
            userId: alice.id,
          );

      await c.read(authProvider.notifier).sessionExpired();

      expect(c.read(authProvider).value, const SignedOut());
      expect(c.read(authProvider).error, isA<SessionExpired>());
      expect(plain, isEmpty);
      expect(await outboxCount(), 1);
    });

    test('sign out discards the outbox', () async {
      final c = await signedIn();
      await c
          .read(spotRepositoryProvider)
          .editSpot(
            's1',
            name: 'x',
            description: '',
            openNow: true,
            userId: alice.id,
          );

      await c.read(authProvider.notifier).logout();

      expect(c.read(authProvider).value, const SignedOut());
      expect(await outboxCount(), 0);
    });

    test('another user signing in discards the first user\'s edits', () async {
      final c = await signedIn();
      await c
          .read(spotRepositoryProvider)
          .editSpot(
            's1',
            name: 'x',
            description: '',
            openNow: true,
            userId: alice.id,
          );
      await c.read(authProvider.notifier).sessionExpired();

      api.nextUser = bob;
      await c.read(authProvider.notifier).login('bob@x', 'password');

      expect(c.read(authProvider).value, const SignedIn(bob));
      expect(await outboxCount(), 0);
    });

    test('unlock: cancel stays Locked, success signs in', () async {
      final first = await signedIn();
      await first.read(biometricSettingProvider.future);
      await first.read(biometricSettingProvider.notifier).set(true);
      final c = start();
      expect(await c.read(authProvider.future), const Locked(alice));

      biometric.failRead = androidBiometricError(13);
      await c.read(authProvider.notifier).unlock();
      expect(c.read(authProvider), const AsyncData<Session>(Locked(alice)));

      biometric.failRead = null;
      await c.read(authProvider.notifier).unlock();
      expect(c.read(authProvider).value, const SignedIn(alice));
    });

    test('unlock after a biometric change: SignedOut with '
        'BiometricInvalidated, outbox kept', () async {
      final first = await signedIn();
      await first.read(biometricSettingProvider.future);
      await first.read(biometricSettingProvider.notifier).set(true);
      await first
          .read(spotRepositoryProvider)
          .editSpot(
            's1',
            name: 'x',
            description: '',
            openNow: true,
            userId: alice.id,
          );
      biometric.values.clear();
      final c = start();
      await c.read(authProvider.future);

      await c.read(authProvider.notifier).unlock();

      expect(c.read(authProvider).value, const SignedOut());
      expect(c.read(authProvider).error, isA<BiometricInvalidated>());
      expect(await outboxCount(), 1);
    });

    test('"Use password instead" signs out and discards the outbox', () async {
      final first = await signedIn();
      await first.read(biometricSettingProvider.future);
      await first.read(biometricSettingProvider.notifier).set(true);
      await first
          .read(spotRepositoryProvider)
          .editSpot(
            's1',
            name: 'x',
            description: '',
            openNow: true,
            userId: alice.id,
          );
      final c = start();
      expect(await c.read(authProvider.future), const Locked(alice));

      await c.read(authProvider.notifier).logout();

      expect(c.read(authProvider).value, const SignedOut());
      expect(biometric.values, isEmpty);
      expect(await outboxCount(), 0);
    });
  });

  group('BiometricSettingNotifier', () {
    test('off by default; a cancelled prompt keeps it off with the '
        'error', () async {
      final c = await signedIn();
      c.listen(biometricSettingProvider, (_, _) {});
      expect(await c.read(biometricSettingProvider.future), isFalse);
      expect(await c.read(biometricAvailableProvider.future), isTrue);

      biometric.failWrite = androidBiometricError(13);
      await c.read(biometricSettingProvider.notifier).set(true);

      expect(c.read(biometricSettingProvider).value, isFalse);
      expect(c.read(biometricSettingProvider).error, isA<BiometricCancelled>());

      biometric.failWrite = null;
      await c.read(biometricSettingProvider.notifier).set(true);
      expect(c.read(biometricSettingProvider), const AsyncData(true));

      await c.read(biometricSettingProvider.notifier).set(false);
      expect(c.read(biometricSettingProvider), const AsyncData(false));
    });

    test('not offered without a strong biometric', () async {
      final c = start(biometrics: const [BiometricType.weak]);

      expect(await c.read(biometricAvailableProvider.future), isFalse);
    });
  });

  group('EditSpotController', () {
    test('saves the edit as the signed-in user', () async {
      final c = await signedIn();
      c.listen(editSpotProvider('s2'), (_, _) {});

      final saved = await c
          .read(editSpotProvider('s2').notifier)
          .save(name: 'New name', description: '', openNow: true);

      expect(saved, isTrue);
      expect(c.read(editSpotProvider('s2')), const AsyncData<void>(null));
      expect((await db.getSpot('s2'))!.name, 'New name');
      expect((await db.pendingOps()).single.userId, alice.id);
    });

    test('is busy while saving and ignores a double tap', () async {
      final c = await signedIn();
      c.listen(editSpotProvider('s2'), (_, _) {});
      final notifier = c.read(editSpotProvider('s2').notifier);

      final first = notifier.save(name: 'a', description: '', openNow: true);
      expect(c.read(editSpotProvider('s2')).isLoading, isTrue);
      final second = await notifier.save(
        name: 'b',
        description: '',
        openNow: true,
      );

      expect(second, isFalse);
      expect(await first, isTrue);
      expect(await outboxCount(), 1);
      expect((await db.getSpot('s2'))!.name, 'a');
    });

    test('an edit that cannot be stored is an error, not a crash', () async {
      final c = await signedIn();
      c.listen(editSpotProvider('missing'), (_, _) {});

      final saved = await c
          .read(editSpotProvider('missing').notifier)
          .save(name: 'x', description: '', openNow: true);

      expect(saved, isFalse);
      expect(c.read(editSpotProvider('missing')).error, isA<StateError>());
      expect(await outboxCount(), 0);
    });

    test('signed out: error, nothing queued', () async {
      final c = start();
      await c.read(authProvider.future);
      c.listen(editSpotProvider('s2'), (_, _) {});

      final saved = await c
          .read(editSpotProvider('s2').notifier)
          .save(name: 'x', description: '', openNow: true);

      expect(saved, isFalse);
      expect(c.read(editSpotProvider('s2')).error, isA<StateError>());
      expect(await outboxCount(), 0);
    });

    test('closing the sheet mid-save is not an error', () async {
      final repo = GatedSpotRepository();
      final c = ProviderContainer.test(
        overrides: [
          spotRepositoryProvider.overrideWithValue(repo),
          signedInUserProvider.overrideWithValue(alice),
        ],
      );
      final sub = c.listen(editSpotProvider('s2'), (_, _) {});

      final saving = c
          .read(editSpotProvider('s2').notifier)
          .save(name: 'a', description: '', openNow: true);
      sub.close();
      await pumpEventQueue();
      expect(c.read(editSpotProvider('s2').exists), isFalse);
      repo.gate.complete();

      expect(await saving, isFalse);
      expect(repo.edits, ['s2']);
    });
  });

  group('AddReviewController', () {
    test('posts as the signed-in user, ignoring a double tap', () async {
      final c = await signedIn();
      c.listen(addReviewProvider('s1'), (_, _) {});
      final notifier = c.read(addReviewProvider('s1').notifier);

      final first = notifier.submit(stars: 5, text: 'Great');
      final second = await notifier.submit(stars: 5, text: 'Great');

      expect(second, isFalse);
      expect(await first, isTrue);
      final local = await db.watchReviews('s1').first;
      expect(local.single.author, alice.displayName);
      expect(local.single.id, startsWith(localIdPrefix));
      expect((await db.pendingOps()).single.userId, alice.id);
    });

    test('signed out: error, nothing queued', () async {
      final c = start();
      await c.read(authProvider.future);
      c.listen(addReviewProvider('s1'), (_, _) {});

      final posted = await c
          .read(addReviewProvider('s1').notifier)
          .submit(stars: 1, text: '');

      expect(posted, isFalse);
      expect(c.read(addReviewProvider('s1')).error, isA<StateError>());
      expect(await outboxCount(), 0);
    });
  });

  group('DescribeDishController', () {
    final pizza = spot('s3', name: 'Pizza Stop');

    ProviderContainer describing({User? user = alice}) =>
        ProviderContainer.test(
          overrides: [
            apiClientProvider.overrideWithValue(api),
            signedInUserProvider.overrideWithValue(user),
          ],
        );

    test('streams the text, then stops; a second tap while streaming is '
        'ignored', () async {
      final c = describing();
      final states = <DishDescription>[];
      c.listen(
        describeDishProvider('s3'),
        (_, next) => states.add(next),
        fireImmediately: true,
      );
      final notifier = c.read(describeDishProvider('s3').notifier);

      notifier.describe(pizza);
      notifier.describe(pizza);
      final stream = api.describeStreams.single;
      stream
        ..add('Pizza ')
        ..add('by the slice.');
      await stream.close();
      await pumpEventQueue();

      expect(states, [
        const DishDescription(),
        const DishDescription(isStreaming: true),
        const DishDescription(text: 'Pizza ', isStreaming: true),
        const DishDescription(text: 'Pizza by the slice.', isStreaming: true),
        const DishDescription(text: 'Pizza by the slice.'),
      ]);
    });

    test('an error ends the stream, keeps the text, and allows a '
        'retry', () async {
      final c = describing();
      c.listen(describeDishProvider('s3'), (_, _) {});
      final notifier = c.read(describeDishProvider('s3').notifier);

      notifier.describe(pizza);
      api.describeStreams.last
        ..add('Pizza')
        ..addError(const AiUnavailable.cutOff());
      await pumpEventQueue();

      final failed = c.read(describeDishProvider('s3'));
      expect(failed.isStreaming, isFalse);
      expect(failed.text, 'Pizza');
      expect(failed.error, isA<AiUnavailable>());

      notifier.describe(pizza);
      expect(api.describeStreams, hasLength(2));
      expect(
        c.read(describeDishProvider('s3')),
        const DishDescription(isStreaming: true),
      );
    });

    test('signed out: an error and no request', () {
      final c = describing(user: null);
      c.listen(describeDishProvider('s3'), (_, _) {});

      c.read(describeDishProvider('s3').notifier).describe(pizza);

      expect(c.read(describeDishProvider('s3')).error, isA<StateError>());
      expect(api.describeStreams, isEmpty);
    });

    test('leaving the screen cancels the stream', () async {
      final c = describing();
      final sub = c.listen(describeDishProvider('s3'), (_, _) {});
      c.read(describeDishProvider('s3').notifier).describe(pizza);
      expect(api.describeStreams.single.hasListener, isTrue);

      sub.close();
      await pumpEventQueue();

      expect(c.read(describeDishProvider('s3').exists), isFalse);
      expect(api.describeStreams.single.hasListener, isFalse);
    });
  });
}

/// Holds every edit until [gate] completes.
class GatedSpotRepository implements SpotRepository {
  final gate = Completer<void>();
  final edits = <String>[];

  @override
  Future<void> editSpot(
    String id, {
    required String name,
    required String description,
    required bool openNow,
    required String userId,
  }) async {
    edits.add(id);
    await gate.future;
  }

  @override
  Future<void> syncOutbox() async {}

  @override
  Object? noSuchMethod(Invocation invocation) => super.noSuchMethod(invocation);
}
