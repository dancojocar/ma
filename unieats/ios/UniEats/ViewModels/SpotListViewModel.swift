import Foundation

@MainActor
@Observable
final class SpotListViewModel {
    private(set) var spots: [Spot] = sampleSpots
    private(set) var searchQuery = ""
    private(set) var categoryFilter: String?
    private(set) var favouriteIds: Set<String> = []

    var filteredSpots: [Spot] {
        spots.filter { spot in
            let matchesQuery = searchQuery.isEmpty || spot.name.localizedCaseInsensitiveContains(searchQuery)
            let matchesCategory = categoryFilter == nil || spot.category == categoryFilter
            return matchesQuery && matchesCategory
        }
    }

    func onQueryChange(_ query: String) {
        searchQuery = query
    }

    func onCategoryChange(_ category: String?) {
        categoryFilter = category
    }

    func toggleFavourite(_ spotId: String) {
        if favouriteIds.contains(spotId) {
            favouriteIds.remove(spotId)
        } else {
            favouriteIds.insert(spotId)
        }
    }
}
