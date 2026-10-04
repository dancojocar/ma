import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:local_auth/local_auth.dart';

import '../data/auth/auth_interceptor.dart';
import '../data/auth/auth_repository.dart';
import '../data/auth/token_store.dart';
import '../data/database/database.dart';
import '../data/network/api_client.dart';
import '../data/network/api_config.dart';
import '../data/network/live_updates.dart';
import '../data/repository/review_repository.dart';
import '../data/repository/spot_repository.dart';
import '../domain/models.dart';

final databaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(db.close);
  return db;
});

final tokenStoreProvider = Provider<TokenStore>((_) => TokenStore());

final dioProvider = Provider<Dio>((ref) {
  final dio = createDio(apiBaseUrl);
  dio.interceptors.insert(
    0,
    AuthInterceptor(
      ref.watch(tokenStoreProvider),
      onUnauthorized: () => ref.read(authProvider.notifier).sessionExpired(),
    ),
  );
  ref.onDispose(dio.close);
  return dio;
});

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(ref.watch(dioProvider)),
);

final localAuthProvider = Provider<LocalAuthentication>(
  (_) => LocalAuthentication(),
);

final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepository(
    ref.watch(apiClientProvider),
    ref.watch(tokenStoreProvider),
    ref.watch(spotRepositoryProvider),
    ref.watch(localAuthProvider),
  ),
);

/// The router sends `SignedOut` to `/login` and `Locked` to `/unlock`.
class AuthNotifier extends AsyncNotifier<Session> {
  AuthRepository get _repo => ref.read(authRepositoryProvider);

  @override
  Future<Session> build() => _repo.restoreSession();

  Future<void> login(String email, String password) async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(
      () async => SignedIn(await _repo.login(email, password)),
    );
    if (state.value is SignedIn) _syncOutbox();
  }

  Future<void> unlock() async {
    final locked = state.value;
    if (locked is! Locked || state.isLoading) return;
    state = const AsyncLoading();
    try {
      state = AsyncData(SignedIn(await _repo.unlock()));
      _syncOutbox();
    } on BiometricCancelled {
      state = AsyncData(locked);
    } on Exception catch (e, stackTrace) {
      _endWith(e, stackTrace);
    }
  }

  /// Sign out, also "Use password instead" on the unlock screen: unsynced
  /// local changes are discarded.
  Future<void> logout() async {
    await _repo.logout();
    state = const AsyncData(SignedOut());
  }

  /// A mutation came back 401: the token expired or was revoked. Queued edits
  /// stay for when the same user signs in again.
  Future<void> sessionExpired() async {
    if (state.value is! SignedIn) return;
    await _repo.endSession();
    _endWith(const SessionExpired(), StackTrace.current);
  }

  void _endWith(Object error, StackTrace stackTrace) {
    // Riverpod keeps the previous value next to an error; set SignedOut first
    // so the old session is gone, not just hidden behind the error.
    state = const AsyncData(SignedOut());
    state = AsyncError(error, stackTrace);
  }

  void _syncOutbox() =>
      unawaited(ref.read(spotRepositoryProvider).syncOutbox());
}

final authProvider = AsyncNotifierProvider<AuthNotifier, Session>(
  AuthNotifier.new,
);

final signedInUserProvider = Provider<User?>(
  (ref) => switch (ref.watch(authProvider).value) {
    SignedIn(:final user) => user,
    _ => null,
  },
);

/// Whether to offer "Unlock with biometrics" at all.
final biometricAvailableProvider = FutureProvider.autoDispose<bool>(
  (ref) => ref.watch(authRepositoryProvider).biometricsAvailable(),
);

/// The "Unlock with biometrics" switch. Off until the user turns it on.
class BiometricSettingNotifier extends AsyncNotifier<bool> {
  @override
  Future<bool> build() => ref.read(authRepositoryProvider).biometricEnabled();

  /// A failure (prompt cancelled, Android older than 9) keeps the old value
  /// and leaves the error in `state` for a SnackBar.
  Future<void> set(bool enabled) async {
    if (state.isLoading) return;
    state = const AsyncLoading();
    final repo = ref.read(authRepositoryProvider);
    final result = await AsyncValue.guard(() async {
      enabled ? await repo.enableBiometric() : await repo.disableBiometric();
      return enabled;
    });
    if (ref.mounted) state = result;
  }
}

final biometricSettingProvider =
    AsyncNotifierProvider.autoDispose<BiometricSettingNotifier, bool>(
      BiometricSettingNotifier.new,
    );

final spotRepositoryProvider = Provider<SpotRepository>(
  (ref) =>
      SpotRepository(ref.watch(databaseProvider), ref.watch(apiClientProvider)),
);

final reviewRepositoryProvider = Provider<ReviewRepository>(
  (ref) => ReviewRepository(
    ref.watch(databaseProvider),
    ref.watch(apiClientProvider),
  ),
);

final connectivityProvider = Provider<Connectivity>((_) => Connectivity());

/// Replays the outbox at start-up and whenever the device regains a network
/// connection. Kept alive by the app root.
final outboxSyncProvider = Provider<void>((ref) {
  final repo = ref.watch(spotRepositoryProvider);
  unawaited(repo.syncOutbox());
  final sub = ref.watch(connectivityProvider).onConnectivityChanged.listen((
    results,
  ) {
    if (results.any((r) => r != ConnectivityResult.none)) {
      unawaited(repo.syncOutbox());
    }
  });
  ref.onDispose(sub.cancel);
});

class AppLifecycleNotifier extends Notifier<AppLifecycleState> {
  @override
  AppLifecycleState build() {
    final listener = AppLifecycleListener(onStateChange: (s) => state = s);
    ref.onDispose(listener.dispose);
    return WidgetsBinding.instance.lifecycleState ?? AppLifecycleState.resumed;
  }
}

final appLifecycleProvider =
    NotifierProvider<AppLifecycleNotifier, AppLifecycleState>(
      AppLifecycleNotifier.new,
    );

/// Open only while someone watches it (autoDispose) and the app is in the
/// foreground; going to the background closes the socket, coming back
/// reconnects.
final liveEventsProvider = StreamProvider.autoDispose<LiveEvent>((ref) {
  final foreground = ref.watch(
    appLifecycleProvider.select(
      (s) => s == AppLifecycleState.resumed || s == AppLifecycleState.inactive,
    ),
  );
  if (!foreground) return const Stream.empty();
  return liveSpotEvents(liveUrl);
});

class SpotListUiState {
  const SpotListUiState({
    this.searchQuery = '',
    this.categoryFilter,
    this.favouriteIds = const {},
  });

  final String searchQuery;
  final SpotCategory? categoryFilter;
  final Set<String> favouriteIds;

  SpotListUiState copyWith({String? searchQuery, Set<String>? favouriteIds}) =>
      SpotListUiState(
        searchQuery: searchQuery ?? this.searchQuery,
        categoryFilter: categoryFilter,
        favouriteIds: favouriteIds ?? this.favouriteIds,
      );

  @override
  bool operator ==(Object other) =>
      other is SpotListUiState &&
      other.searchQuery == searchQuery &&
      other.categoryFilter == categoryFilter &&
      setEquals(other.favouriteIds, favouriteIds);

  @override
  int get hashCode => Object.hash(
    searchQuery,
    categoryFilter,
    Object.hashAllUnordered(favouriteIds),
  );
}

class SpotListNotifier extends Notifier<SpotListUiState> {
  @override
  SpotListUiState build() => const SpotListUiState();

  void setSearchQuery(String query) =>
      state = state.copyWith(searchQuery: query);

  void setCategoryFilter(SpotCategory? category) => state = SpotListUiState(
    searchQuery: state.searchQuery,
    categoryFilter: category,
    favouriteIds: state.favouriteIds,
  );

  void toggleFavourite(String spotId) {
    final ids = state.favouriteIds;
    state = state.copyWith(
      favouriteIds: ids.contains(spotId)
          ? ({...ids}..remove(spotId))
          : {...ids, spotId},
    );
  }
}

final spotListProvider = NotifierProvider<SpotListNotifier, SpotListUiState>(
  SpotListNotifier.new,
);

class SpotPaging {
  const SpotPaging({
    required this.page,
    required this.hasNextPage,
    this.isLoadingMore = false,
    this.loadMoreError,
  });

  final int page;
  final bool hasNextPage;
  final bool isLoadingMore;
  final Object? loadMoreError;
}

/// Page-by-page `GET /spots` for the current search query and category; every
/// page is upserted into the local database, which is what the list shows.
/// Changing either filter starts again from page 1.
class SpotPagingNotifier extends AsyncNotifier<SpotPaging> {
  static const pageSize = 20;

  int _generation = 0;

  @override
  Future<SpotPaging> build() async {
    _generation++;
    final (query, category) = ref.watch(
      spotListProvider.select((s) => (s.searchQuery.trim(), s.categoryFilter)),
    );
    final cancelToken = CancelToken();
    ref.onDispose(cancelToken.cancel);

    if (query.isNotEmpty) {
      await Future<void>.delayed(const Duration(milliseconds: 300));
    }
    final first = await ref
        .read(spotRepositoryProvider)
        .refreshPage(
          page: 1,
          limit: pageSize,
          query: query,
          category: category,
          cancelToken: cancelToken,
        );
    return SpotPaging(page: first.page, hasNextPage: first.hasNextPage);
  }

  Future<void> loadNextPage() async {
    final current = state.value;
    if (current == null ||
        state.isLoading ||
        !current.hasNextPage ||
        current.isLoadingMore) {
      return;
    }
    final generation = _generation;
    final filter = ref.read(spotListProvider);
    state = AsyncData(
      SpotPaging(page: current.page, hasNextPage: true, isLoadingMore: true),
    );
    try {
      final next = await ref
          .read(spotRepositoryProvider)
          .refreshPage(
            page: current.page + 1,
            limit: pageSize,
            query: filter.searchQuery.trim(),
            category: filter.categoryFilter,
          );
      if (!ref.mounted || generation != _generation) return;
      state = AsyncData(
        SpotPaging(page: next.page, hasNextPage: next.hasNextPage),
      );
    } on Exception catch (e) {
      if (!ref.mounted || generation != _generation) return;
      state = AsyncData(
        SpotPaging(page: current.page, hasNextPage: true, loadMoreError: e),
      );
    }
  }
}

final spotPagingProvider =
    AsyncNotifierProvider.autoDispose<SpotPagingNotifier, SpotPaging>(
      SpotPagingNotifier.new,
    );

/// What the list renders: local rows matching the current filters.
final visibleSpotsProvider = StreamProvider.autoDispose<List<Spot>>((ref) {
  final (query, category) = ref.watch(
    spotListProvider.select((s) => (s.searchQuery.trim(), s.categoryFilter)),
  );
  return ref
      .watch(spotRepositoryProvider)
      .watchSpots(query: query, category: category);
});

/// Writes live WebSocket events into the database (the list and detail
/// screens update from there) and replays the outbox whenever the socket
/// (re)connects, because then the server is reachable.
final liveSyncProvider = Provider.autoDispose<void>((ref) {
  final repo = ref.watch(spotRepositoryProvider);
  ref.listen(liveEventsProvider, (_, next) {
    switch (next.value) {
      case LiveConnected():
        unawaited(repo.syncOutbox());
      case final event?:
        unawaited(repo.applyLiveEvent(event));
      case null:
        break;
    }
  });
});

final pendingSpotIdsProvider = StreamProvider.autoDispose<Set<String>>(
  (ref) => ref.watch(spotRepositoryProvider).watchPendingIds(),
);

final pendingChangesCountProvider = StreamProvider.autoDispose<int>(
  (ref) => ref.watch(spotRepositoryProvider).watchPendingCount(),
);

final spotDetailProvider = StreamProvider.autoDispose.family<Spot?, String>(
  (ref, id) => ref.watch(spotRepositoryProvider).watchSpot(id),
);

final spotRefreshProvider = FutureProvider.autoDispose.family<void, String>(
  (ref, id) => ref.watch(spotRepositoryProvider).refreshSpot(id),
);

final reviewsProvider = StreamProvider.autoDispose.family<List<Review>, String>(
  (ref, spotId) => ref.watch(reviewRepositoryProvider).watchReviews(spotId),
);

final reviewsRefreshProvider = FutureProvider.autoDispose.family<void, String>(
  (ref, spotId) => ref.watch(reviewRepositoryProvider).refreshReviews(spotId),
);

class EditSpotController extends AsyncNotifier<void> {
  EditSpotController(this.spotId);

  final String spotId;

  @override
  FutureOr<void> build() {}

  /// Returns false while a save is already running or when it fails; the
  /// failure is then in `state`.
  Future<bool> save({
    required String name,
    required String description,
    required bool openNow,
  }) async {
    if (state.isLoading) return false;
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final user = ref.read(signedInUserProvider);
      if (user == null) throw StateError('Not signed in');
      final repo = ref.read(spotRepositoryProvider);
      await repo.editSpot(
        spotId,
        name: name,
        description: description,
        openNow: openNow,
        userId: user.id,
      );
      unawaited(repo.syncOutbox());
    });
    if (!ref.mounted) return false;
    state = result;
    return !result.hasError;
  }
}

final editSpotProvider = AsyncNotifierProvider.autoDispose
    .family<EditSpotController, void, String>(EditSpotController.new);

class AddReviewController extends AsyncNotifier<void> {
  AddReviewController(this.spotId);

  final String spotId;

  @override
  FutureOr<void> build() {}

  /// Returns false while a submit is already running or when it fails; the
  /// failure is then in `state`.
  Future<bool> submit({required int stars, required String text}) async {
    if (state.isLoading) return false;
    state = const AsyncLoading();
    final result = await AsyncValue.guard(() async {
      final user = ref.read(signedInUserProvider);
      if (user == null) throw StateError('Not signed in');
      await ref
          .read(reviewRepositoryProvider)
          .addReview(spotId, stars: stars, text: text, author: user);
      unawaited(ref.read(spotRepositoryProvider).syncOutbox());
    });
    if (!ref.mounted) return false;
    state = result;
    return !result.hasError;
  }
}

final addReviewProvider = AsyncNotifierProvider.autoDispose
    .family<AddReviewController, void, String>(AddReviewController.new);
