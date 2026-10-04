import Foundation

@MainActor
@Observable
final class SpotListViewModel {
    private(set) var spots: [Spot] = []
    private(set) var searchQuery = ""
    private(set) var categoryFilter: String?
    private(set) var favouriteIds: Set<String> = []
    private(set) var isLoading = false
    private(set) var isLoadingMore = false
    private(set) var errorMessage: String?
    private(set) var hasNextPage = true
    private(set) var currentPage = 0
    private(set) var isLive = false

    private let api: ApiClient
    private var reloadTask: Task<Void, Never>?

    init(api: ApiClient) {
        self.api = api
    }

    func onQueryChange(_ query: String) {
        guard query != searchQuery else { return }
        searchQuery = query
        scheduleReload(debounce: .milliseconds(300))
    }

    func onCategoryChange(_ category: String?) {
        guard category != categoryFilter else { return }
        categoryFilter = category
        scheduleReload(debounce: .zero)
    }

    func toggleFavourite(_ spotId: String) {
        if favouriteIds.contains(spotId) {
            favouriteIds.remove(spotId)
        } else {
            favouriteIds.insert(spotId)
        }
    }

    func refresh() async {
        reloadTask?.cancel()
        await loadFirstPage()
    }

    private func loadFirstPage() async {
        isLoading = true
        defer { isLoading = false }
        errorMessage = nil
        do {
            let page = try await api.listSpots(page: 1, query: searchQuery, category: categoryFilter)
            spots = page.spots.map(Spot.init(dto:))
            currentPage = 1
            hasNextPage = page.hasNextPage
        } catch is CancellationError {
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func loadMoreIfNeeded(after spot: Spot) async {
        guard spot.id == spots.last?.id else { return }
        await loadMore()
    }

    func loadMore() async {
        guard hasNextPage, !isLoading, !isLoadingMore else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        errorMessage = nil
        do {
            let page = try await api.listSpots(page: currentPage + 1, query: searchQuery, category: categoryFilter)
            let known = Set(spots.map(\.id))
            spots += page.spots.map(Spot.init(dto:)).filter { !known.contains($0.id) }
            currentPage = page.page
            hasNextPage = page.hasNextPage && !page.spots.isEmpty
        } catch is CancellationError {
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func apply(_ update: LiveUpdate) {
        switch update {
        case .connected:
            isLive = true
        case .disconnected:
            isLive = false
        case .spotCreated(let spot):
            if matchesFilters(spot), !spots.contains(where: { $0.id == spot.id }) {
                spots.insert(spot, at: 0)
            }
        case .spotUpdated(let spot):
            if let index = spots.firstIndex(where: { $0.id == spot.id }) {
                spots[index] = spot
            }
        case .spotDeleted(let id):
            spots.removeAll { $0.id == id }
        }
    }

    private func matchesFilters(_ spot: Spot) -> Bool {
        (searchQuery.isEmpty || spot.name.localizedCaseInsensitiveContains(searchQuery))
            && (categoryFilter == nil || spot.category == categoryFilter)
    }

    private func scheduleReload(debounce: Duration) {
        reloadTask?.cancel()
        reloadTask = Task {
            try? await Task.sleep(for: debounce)
            guard !Task.isCancelled else { return }
            await loadFirstPage()
        }
    }
}
