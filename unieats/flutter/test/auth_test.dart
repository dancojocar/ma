import 'dart:convert';

import 'package:drift/drift.dart' show driftRuntimeOptions;
import 'package:drift/native.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:local_auth/local_auth.dart';
import 'package:unieats/data/auth/auth_repository.dart';
import 'package:unieats/data/auth/token_store.dart';
import 'package:unieats/data/database/database.dart';
import 'package:unieats/data/repository/review_repository.dart';
import 'package:unieats/data/repository/spot_repository.dart';
import 'package:unieats_data/unieats_data.dart';

import 'support/fakes.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  driftRuntimeOptions.dontWarnAboutMultipleDatabases = true;

  late Map<String, String> plain;
  late FakeBiometricStorage biometric;
  late ScriptedApi api;
  late AppDatabase db;
  late DriftSpotRepository spots;
  late DriftReviewRepository reviews;
  late TokenStore tokens;
  late TokenAuthRepository auth;

  /// A fresh app start: nothing in memory, the same stores on the device.
  TokenAuthRepository coldStart([List<BiometricType>? types]) =>
      TokenAuthRepository(
        api,
        TokenStore(null, biometric),
        spots,
        FakeLocalAuth(types ?? const [BiometricType.strong]),
      );

  setUp(() async {
    plain = {};
    FlutterSecureStorage.setMockInitialValues(plain);
    biometric = FakeBiometricStorage();
    api = ScriptedApi();
    db = AppDatabase.forTesting(NativeDatabase.memory());
    spots = DriftSpotRepository(db, api);
    reviews = DriftReviewRepository(db, api);
    tokens = TokenStore(null, biometric);
    auth = TokenAuthRepository(api, tokens, spots, FakeLocalAuth());
    await db.upsertFromServer([spot('s1'), spot('s2'), spot('s3')]);
  });
  tearDown(() => db.close());

  Future<void> edit(String id, User user, {String name = 'edited'}) =>
      spots.editSpot(
        id,
        name: name,
        description: '',
        openNow: true,
        userId: user.id,
      );

  Future<int> outboxCount() async => (await db.pendingOps()).length;

  group('restoring the session', () {
    test('nothing stored -> SignedOut', () async {
      expect(await auth.restoreSession(), const SignedOut());
    });

    test('a valid token -> SignedIn', () async {
      await auth.login('alice@unieats.app', 'password');

      expect(await coldStart().restoreSession(), const SignedIn(alice));
    });

    test('an expired token -> SignedOut, and the store is cleared', () async {
      await tokens.write(
        jwt(validFor: const Duration(hours: -1)),
        userJson: jsonEncode(alice.toJson()),
      );

      expect(await auth.restoreSession(), const SignedOut());
      expect(plain, isEmpty);
    });
  });

  group('biometric unlock', () {
    test('available only with a strong biometric enrolled', () async {
      Future<bool> availableWith(List<BiometricType> types) =>
          coldStart(types).biometricsAvailable();

      expect(await availableWith([BiometricType.strong]), isTrue);
      expect(await availableWith([BiometricType.face]), isTrue);
      expect(await availableWith([BiometricType.weak]), isFalse);
      expect(await availableWith(const []), isFalse);
    });

    test('off by default', () async {
      await auth.login('alice@unieats.app', 'password');

      expect(await auth.biometricEnabled(), isFalse);
      expect(biometric.values, isEmpty);
    });

    test('enable moves the token into the biometric store; a cold start is '
        'Locked until unlock', () async {
      await auth.login('alice@unieats.app', 'password');
      final token = plain['jwt'];

      await auth.enableBiometric();

      expect(await auth.biometricEnabled(), isTrue);
      expect(biometric.values['jwt_biometric'], token);
      expect(plain['jwt'], isNull);
      expect(await tokens.read(), token, reason: 'kept in memory, no prompt');

      final restarted = TokenStore(null, biometric);
      final repo = TokenAuthRepository(api, restarted, spots, FakeLocalAuth());
      expect(await repo.restoreSession(), const Locked(alice));
      expect(await restarted.read(), isNull, reason: 'locked: no token yet');
      expect(await repo.unlock(), alice);
      expect(await restarted.read(), token);
    });

    test('disable moves the token back to the normal store', () async {
      await auth.login('alice@unieats.app', 'password');
      final token = plain['jwt'];
      await auth.enableBiometric();

      await auth.disableBiometric();

      expect(await auth.biometricEnabled(), isFalse);
      expect(biometric.values, isEmpty);
      expect(plain['jwt'], token);
      expect(await coldStart().restoreSession(), const SignedIn(alice));
    });

    test('a cancelled enable prompt changes nothing', () async {
      await auth.login('alice@unieats.app', 'password');
      final token = plain['jwt'];
      biometric.failWrite = androidBiometricError(10);

      await expectLater(
        auth.enableBiometric(),
        throwsA(isA<BiometricCancelled>()),
      );
      expect(plain['jwt'], token);
      expect(await auth.biometricEnabled(), isFalse);

      biometric.failWrite = androidBiometricError(1);
      await expectLater(
        auth.enableBiometric(),
        throwsA(isA<PlatformException>()),
      );
      expect(await auth.biometricEnabled(), isFalse);
    });

    test('a cancelled unlock prompt stays Locked', () async {
      await auth.login('alice@unieats.app', 'password');
      await auth.enableBiometric();
      for (final cancel in [
        androidBiometricError(10),
        androidBiometricError(13),
        PlatformException(code: 'auth', details: -128),
      ]) {
        biometric.failRead = cancel;
        final repo = coldStart();

        await expectLater(repo.unlock(), throwsA(isA<BiometricCancelled>()));
        expect(await repo.restoreSession(), const Locked(alice));
      }
    });

    test('a new enrollment invalidates the key: signed out, queued edits '
        'kept', () async {
      await auth.login('alice@unieats.app', 'password');
      await edit('s1', alice);
      await auth.enableBiometric();
      biometric.values.clear();
      final repo = coldStart();

      await expectLater(repo.unlock(), throwsA(isA<BiometricInvalidated>()));
      expect(await repo.restoreSession(), const SignedOut());
      expect(plain, isEmpty);
      expect(await outboxCount(), 1);
    });

    test('an iOS item that is no longer found is invalidated too', () async {
      await auth.login('alice@unieats.app', 'password');
      await auth.enableBiometric();
      biometric.failRead = PlatformException(code: 'auth', details: -25300);

      await expectLater(
        coldStart().unlock(),
        throwsA(isA<BiometricInvalidated>()),
      );
    });

    test('unlocking an expired token -> SessionExpired, both stores '
        'cleared', () async {
      api.token = jwt(validFor: const Duration(hours: -1));
      await auth.login('alice@unieats.app', 'password');
      await auth.enableBiometric();
      final repo = coldStart();
      expect(await repo.restoreSession(), const Locked(alice));

      await expectLater(repo.unlock(), throwsA(isA<SessionExpired>()));
      expect(plain, isEmpty);
      expect(biometric.values, isEmpty);
    });
  });

  group('sign-out and the outbox', () {
    test('sign out clears both stores and discards every unsynced '
        'change', () async {
      await auth.login('alice@unieats.app', 'password');
      await auth.enableBiometric();
      await edit('s1', alice);
      await reviews.addReview('s2', stars: 4, text: 'ok', author: alice);
      expect(await outboxCount(), 2);

      await auth.logout();

      expect(plain, isEmpty);
      expect(biometric.values, isEmpty);
      expect(await outboxCount(), 0);
      expect(await db.getSpot('s1'), isNull, reason: 'refetched later');
      expect(await db.getSpot('s2'), isNotNull);
      expect(await db.watchReviews('s2').first, isEmpty);
    });

    test('an expired session (401) keeps queued edits', () async {
      await auth.login('alice@unieats.app', 'password');
      await edit('s1', alice);

      await auth.endSession();

      expect(plain, isEmpty);
      expect(await outboxCount(), 1);
      expect((await db.getSpot('s1'))!.name, 'edited');
    });

    test('ops record the user who made them', () async {
      await edit('s1', alice);
      await reviews.addReview('s2', stars: 4, text: 'ok', author: alice);

      expect((await db.pendingOps()).map((op) => op.userId), [
        alice.id,
        alice.id,
      ]);
    });

    test('the same user signing in again keeps them', () async {
      await auth.login('alice@unieats.app', 'password');
      await edit('s1', alice);
      await auth.endSession();

      await auth.login('alice@unieats.app', 'password');

      expect(await outboxCount(), 1);
      expect((await db.getSpot('s1'))!.pendingSync, isTrue);
    });

    test('another user signing in discards the other user\'s ops '
        'only', () async {
      await auth.login('alice@unieats.app', 'password');
      await edit('s1', alice);
      await reviews.addReview('s2', stars: 4, text: 'ok', author: alice);
      await edit('s3', bob, name: 'by bob');
      await db
          .into(db.outbox)
          .insert(
            OutboxCompanion.insert(
              opId: 'legacy',
              type: OutboxType.updateSpot,
              entityId: 's2',
              payload: '{}',
              createdAt: 0,
            ),
          );
      await auth.endSession();
      api.nextUser = bob;

      await auth.login('bob@unieats.app', 'password');

      final ops = await db.pendingOps();
      expect(ops.map((op) => op.entityId), unorderedEquals(['s3', 's2']));
      expect(ops.map((op) => op.userId), unorderedEquals([bob.id, null]));
      expect(await db.getSpot('s1'), isNull);
      expect(await db.watchReviews('s2').first, isEmpty);
      expect((await db.getSpot('s3'))!.name, 'by bob');
      expect(await tokens.readUser(), contains(bob.id));
    });
  });
}
