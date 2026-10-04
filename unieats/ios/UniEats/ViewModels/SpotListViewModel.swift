import Foundation

@MainActor
@Observable
final class SpotListViewModel {
    private(set) var searchQuery = ""
    private(set) var categoryFilter: String?
    private(set) var favouriteIds: Set<String> = []
    private(set) var isLoading = false
    private(set) var isLoadingMore = false
    private(set) var errorMessage: String?
    private(set) var hasNextPage = true
    private(set) var currentPage = 0
    private(set) var isLive = false

    private let repository: SpotRepository
    private var reloadTask: Task<Void, Never>?

    init(repository: SpotRepository) {
        self.repository = repository
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

    func loadMore() async {
        guard hasNextPage, !isLoading, !isLoadingMore else { return }
        isLoadingMore = true
        defer { isLoadingMore = false }
        errorMessage = nil
        do {
            let page = try await repository.fetchPage(currentPage + 1, query: searchQuery, category: categoryFilter)
            currentPage = page.page
            hasNextPage = page.hasNextPage && !page.spots.isEmpty
        } catch is CancellationError {
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func apply(_ update: LiveUpdate) async {
        switch update {
        case .connected:
            isLive = true
            await repository.sync()
            if errorMessage != nil { await refresh() }
        case .disconnected:
            isLive = false
        default:
            repository.apply(update)
        }
    }

    private func loadFirstPage() async {
        isLoading = true
        defer { isLoading = false }
        errorMessage = nil
        do {
            let page = try await repository.fetchPage(1, query: searchQuery, category: categoryFilter)
            currentPage = 1
            hasNextPage = page.hasNextPage
        } catch is CancellationError {
        } catch {
            errorMessage = error.localizedDescription
        }
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
