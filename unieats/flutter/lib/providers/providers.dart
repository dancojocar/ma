import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/network/api_client.dart';
import '../data/network/api_config.dart';
import '../domain/models.dart';

final dioProvider = Provider<Dio>((ref) {
  final dio = createDio(apiBaseUrl);
  ref.onDispose(dio.close);
  return dio;
});

final apiClientProvider = Provider<ApiClient>(
  (ref) => ApiClient(ref.watch(dioProvider)),
);

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

class SpotPages {
  const SpotPages({
    required this.query,
    required this.category,
    required this.spots,
    required this.page,
    required this.hasNextPage,
    this.isLoadingMore = false,
    this.loadMoreError,
  });

  final String query;
  final SpotCategory? category;
  final List<Spot> spots;
  final int page;
  final bool hasNextPage;
  final bool isLoadingMore;
  final Object? loadMoreError;
}

/// Page-by-page `GET /spots` for the current search query and category.
/// Changing either filter rebuilds it from page 1.
class SpotPagesNotifier extends AsyncNotifier<SpotPages> {
  static const pageSize = 20;

  int _generation = 0;

  @override
  Future<SpotPages> build() async {
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
        .read(apiClientProvider)
        .fetchSpots(
          limit: pageSize,
          query: query,
          category: category?.name,
          cancelToken: cancelToken,
        );
    return SpotPages(
      query: query,
      category: category,
      spots: first.spots,
      page: first.page,
      hasNextPage: first.hasNextPage,
    );
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
    state = AsyncData(_copy(current, isLoadingMore: true));

    try {
      final next = await ref
          .read(apiClientProvider)
          .fetchSpots(
            page: current.page + 1,
            limit: pageSize,
            query: current.query,
            category: current.category?.name,
          );
      if (generation != _generation) return;
      state = AsyncData(
        SpotPages(
          query: current.query,
          category: current.category,
          spots: [...current.spots, ...next.spots],
          page: next.page,
          hasNextPage: next.hasNextPage,
        ),
      );
    } on Exception catch (e) {
      if (generation != _generation) return;
      state = AsyncData(_copy(current, loadMoreError: e));
    }
  }

  SpotPages _copy(
    SpotPages s, {
    bool isLoadingMore = false,
    Object? loadMoreError,
  }) => SpotPages(
    query: s.query,
    category: s.category,
    spots: s.spots,
    page: s.page,
    hasNextPage: s.hasNextPage,
    isLoadingMore: isLoadingMore,
    loadMoreError: loadMoreError,
  );
}

final spotPagesProvider = AsyncNotifierProvider<SpotPagesNotifier, SpotPages>(
  SpotPagesNotifier.new,
);

final spotDetailProvider = FutureProvider.autoDispose.family<Spot, String>(
  (ref, id) => ref.watch(apiClientProvider).fetchSpot(id),
);

final reviewsProvider = FutureProvider.autoDispose.family<List<Review>, String>(
  (ref, spotId) => ref.watch(apiClientProvider).fetchReviews(spotId),
);
