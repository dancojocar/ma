import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/seed_data.dart';
import '../domain/models.dart';

class SpotListUiState {
  const SpotListUiState({
    required this.spots,
    this.searchQuery = '',
    this.categoryFilter,
    this.favouriteIds = const {},
  });

  final List<Spot> spots;
  final String searchQuery;
  final SpotCategory? categoryFilter;
  final Set<String> favouriteIds;

  List<Spot> get visibleSpots {
    final query = searchQuery.trim().toLowerCase();
    return spots.where((spot) {
      final matchesCategory =
          categoryFilter == null || spot.category == categoryFilter;
      final matchesQuery =
          query.isEmpty ||
          spot.name.toLowerCase().contains(query) ||
          spot.description.toLowerCase().contains(query);
      return matchesCategory && matchesQuery;
    }).toList();
  }

  SpotListUiState copyWith({String? searchQuery, Set<String>? favouriteIds}) =>
      SpotListUiState(
        spots: spots,
        searchQuery: searchQuery ?? this.searchQuery,
        categoryFilter: categoryFilter,
        favouriteIds: favouriteIds ?? this.favouriteIds,
      );
}

class SpotListNotifier extends Notifier<SpotListUiState> {
  @override
  SpotListUiState build() => const SpotListUiState(spots: kSeedSpots);

  void setSearchQuery(String query) =>
      state = state.copyWith(searchQuery: query);

  void setCategoryFilter(SpotCategory? category) =>
      state = SpotListUiState(
        spots: state.spots,
        searchQuery: state.searchQuery,
        categoryFilter: category,
        favouriteIds: state.favouriteIds,
      );

  void toggleFavourite(String spotId) {
    final ids = state.favouriteIds;
    state = state.copyWith(
      favouriteIds:
          ids.contains(spotId) ? ({...ids}..remove(spotId)) : {...ids, spotId},
    );
  }
}

final spotListProvider = NotifierProvider<SpotListNotifier, SpotListUiState>(
  SpotListNotifier.new,
);
