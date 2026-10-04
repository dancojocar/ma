import 'dart:async';

import 'package:connectivity_plus/connectivity_plus.dart';
import 'package:dio/dio.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

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

final dioProvider = Provider<Dio>((ref) {
  final dio = createDio(apiBaseUrl);
  ref.onDispose(dio.close);
  return dio;
});

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(ref.watch(dioProvider)),
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

/// Replays the outbox at start-up and whenever the device regains a network
/// connection. Kept alive by the app root.
final outboxSyncProvider = Provider<void>((ref) {
  final repo = ref.watch(spotRepositoryProvider);
  unawaited(repo.syncOutbox());
  final sub = Connectivity().onConnectivityChanged.listen((results) {
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
}

class SpotListNotifier extends Notifier<SpotListUiState> {
  @override
  SpotListUiState build() => const SpotListUiState();

  void setSearchQuery(String query) =>
      state = SpotListUiState(
        searchQuery: query,
        categoryFilter: state.categoryFilter,
        favouriteIds: state.favouriteIds,
      );

  void setCategoryFilter(SpotCategory? category) =>
      state = SpotListUiState(
        searchQuery: state.searchQuery,
        categoryFilter: category,
        favouriteIds: state.favouriteIds,
      );

  void toggleFavourite(String spotId) {
    final ids = state.favouriteIds;
    state = SpotListUiState(
      searchQuery: state.searchQuery,
      categoryFilter: state.categoryFilter,
      favouriteIds:
          ids.contains(spotId) ? ({...ids}..remove(spotId)) : {...ids, spotId},
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
class SpotPagingNotifier extends AutoDisposeAsyncNotifier<SpotPaging> {
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
    final current = state.valueOrNull;
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
      if (generation != _generation) return;
      state = AsyncData(
        SpotPaging(page: next.page, hasNextPage: next.hasNextPage),
      );
    } on Exception catch (e) {
      if (generation != _generation) return;
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
    switch (next.valueOrNull) {
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
