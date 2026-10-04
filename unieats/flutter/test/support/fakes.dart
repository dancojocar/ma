import 'dart:async';
import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:local_auth/local_auth.dart';
import 'package:unieats/data/network/api_client.dart';
import 'package:unieats/data/network/app_error.dart';
import 'package:unieats_data/unieats_data.dart';

const alice = User(
  id: 'u-alice',
  email: 'alice@unieats.app',
  displayName: 'Alice',
);
const bob = User(id: 'u-bob', email: 'bob@unieats.app', displayName: 'Bob');

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

Review review(String id, {String spotId = 'spot-1', String author = 'Ana'}) =>
    Review(
      id: id,
      spotId: spotId,
      author: author,
      stars: 5,
      text: 'Good',
      createdAt: 1,
    );

/// A JWT whose `exp` claim is [validFor] from now; the signature is fake,
/// which is fine because the app only reads the claims.
String jwt({Duration validFor = const Duration(hours: 1)}) {
  String part(Map<String, Object> claims) =>
      base64Url.encode(utf8.encode(jsonEncode(claims))).replaceAll('=', '');
  final exp = DateTime.now().add(validFor).millisecondsSinceEpoch ~/ 1000;
  return '${part({'alg': 'HS256'})}.${part({'exp': exp, 'sub': 'u'})}.sig';
}

/// Answers each call from a queue of scripted results and logs the requests.
///
/// A result is returned when it is a value, awaited when it is a [Future] and
/// thrown otherwise. An empty queue behaves like a device without network.
class ScriptedApi extends ApiClient {
  ScriptedApi() : super(Dio());

  final patchResults = <Object>[];
  final reviewResults = <Object>[];
  final fetchSpotResults = <Object>[];
  Map<String, bool> configFlags = const {};

  User nextUser = alice;
  String token = jwt();
  AppError? loginError;

  /// `PATCH <id>`, `POST <spotId>` and `GET <id>`, in the order they were sent.
  final calls = <String>[];
  final patches = <({String id, Map<String, dynamic> body, String key})>[];
  final reviewKeys = <String>[];
  final describeStreams = <StreamController<String>>[];

  static Future<T> _next<T>(List<Object> results) async {
    if (results.isEmpty) throw const NoConnectivity();
    return switch (results.removeAt(0)) {
      final T value => value,
      final Future<T> future => future,
      final Object error => throw error,
    };
  }

  @override
  Future<({String token, User user})> login(
    String email,
    String password,
  ) async {
    if (loginError case final error?) throw error;
    return (token: token, user: nextUser);
  }

  @override
  Future<Spot> patchSpot(
    String id,
    Map<String, dynamic> patch, {
    required String idempotencyKey,
  }) {
    calls.add('PATCH $id');
    patches.add((id: id, body: patch, key: idempotencyKey));
    return _next<Spot>(patchResults);
  }

  @override
  Future<Review> createReview(
    String spotId, {
    required int stars,
    required String text,
    required String idempotencyKey,
  }) {
    calls.add('POST $spotId');
    reviewKeys.add(idempotencyKey);
    return _next<Review>(reviewResults);
  }

  @override
  Future<Spot> fetchSpot(String id) {
    calls.add('GET $id');
    return _next<Spot>(fetchSpotResults);
  }

  @override
  Future<Map<String, bool>> fetchConfig() async {
    calls.add('GET config');
    return configFlags;
  }

  @override
  Stream<String> describeSpot(Spot spot, {CancelToken? cancelToken}) {
    final controller = StreamController<String>();
    describeStreams.add(controller);
    return controller.stream;
  }
}

class FakeLocalAuth extends LocalAuthentication {
  FakeLocalAuth([this.types = const [BiometricType.strong]]);

  final List<BiometricType> types;

  @override
  Future<bool> get canCheckBiometrics async => types.isNotEmpty;

  @override
  Future<List<BiometricType>> getAvailableBiometrics() async => types;
}

/// The biometric store, backed by a map. [failRead] / [failWrite] stand in
/// for the platform errors the real prompt reports.
class FakeBiometricStorage extends FlutterSecureStorage {
  FakeBiometricStorage();

  final values = <String, String>{};
  PlatformException? failRead;
  PlatformException? failWrite;

  @override
  Future<String?> read({
    required String key,
    AppleOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    AppleOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    if (failRead case final error?) throw error;
    return values[key];
  }

  @override
  Future<void> write({
    required String key,
    required String? value,
    AppleOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    AppleOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    if (failWrite case final error?) throw error;
    values[key] = value!;
  }

  @override
  Future<void> delete({
    required String key,
    AppleOptions? iOptions,
    AndroidOptions? aOptions,
    LinuxOptions? lOptions,
    WebOptions? webOptions,
    AppleOptions? mOptions,
    WindowsOptions? wOptions,
  }) async {
    values.remove(key);
  }
}

/// How `flutter_secure_storage` reports a BiometricPrompt error on Android.
PlatformException androidBiometricError(int code) => PlatformException(
  code: 'Exception encountered',
  message: 'Biometric authentication error [$code]: test',
);

/// Spot data for widget tests: fixed lists, no database, no network.
class InMemorySpotRepository implements SpotRepository {
  InMemorySpotRepository(this.spots);

  final List<Spot> spots;

  @override
  Stream<List<Spot>> watchSpots({String query = '', SpotCategory? category}) =>
      Stream.value([
        for (final s in spots)
          if (s.name.toLowerCase().contains(query.toLowerCase()) &&
              (category == null || s.category == category))
            s,
      ]);

  @override
  Stream<Spot?> watchSpot(String id) =>
      Stream.value(spots.where((s) => s.id == id).firstOrNull);

  @override
  Stream<Set<String>> watchPendingIds() => Stream.value(const {});

  @override
  Stream<int> watchPendingCount() => Stream.value(0);

  @override
  Future<SpotsPage> refreshPage({
    required int page,
    required int limit,
    String query = '',
    SpotCategory? category,
  }) async => SpotsPage(spots: spots, page: page, hasNextPage: false);

  @override
  Future<void> refreshSpot(String id) async {}

  @override
  Future<void> applyLiveEvent(LiveEvent event) async {}

  @override
  Future<void> editSpot(
    String id, {
    required String name,
    required String description,
    required bool openNow,
    required String userId,
  }) async {}

  @override
  Future<void> syncOutbox() async {}

  @override
  Future<void> discardLocalChanges() async {}

  @override
  Future<void> discardChangesNotBy(String userId) async {}
}

class InMemoryReviewRepository implements ReviewRepository {
  InMemoryReviewRepository(this.reviews);

  final List<Review> reviews;

  @override
  Stream<List<Review>> watchReviews(String spotId) => Stream.value([
    for (final r in reviews)
      if (r.spotId == spotId) r,
  ]);

  @override
  Future<void> refreshReviews(String spotId) async {}

  @override
  Future<void> addReview(
    String spotId, {
    required int stars,
    required String text,
    required User author,
  }) async {}
}

/// A session without tokens: [session] is what a cold start restores.
class FakeAuthRepository implements AuthRepository {
  FakeAuthRepository(this.session);

  Session session;
  bool biometricsOnDevice = true;
  bool biometricOn = false;
  Exception? unlockError;
  final calls = <String>[];

  @override
  Future<User> login(String email, String password) async {
    calls.add('login $email');
    session = const SignedIn(alice);
    return alice;
  }

  @override
  Future<void> logout() async {
    calls.add('logout');
    session = const SignedOut();
  }

  @override
  Future<void> endSession() async {
    calls.add('endSession');
    session = const SignedOut();
  }

  @override
  Future<Session> restoreSession() async => session;

  @override
  Future<User> unlock() async {
    calls.add('unlock');
    if (unlockError case final error?) throw error;
    return alice;
  }

  @override
  Future<bool> biometricsAvailable() async => biometricsOnDevice;

  @override
  Future<bool> biometricEnabled() async => biometricOn;

  @override
  Future<void> enableBiometric() async => biometricOn = true;

  @override
  Future<void> disableBiometric() async => biometricOn = false;
}
