import SwiftData
import SwiftUI

struct SpotListView: View {
    let repository: SpotRepository
    let live: LiveUpdateService
    @State private var viewModel: SpotListViewModel
    @State private var path: [String] = []
    @Environment(\.scenePhase) private var scenePhase

    init(repository: SpotRepository, live: LiveUpdateService) {
        self.repository = repository
        self.live = live
        _viewModel = State(initialValue: SpotListViewModel(repository: repository))
    }

    var body: some View {
        NavigationStack(path: $path) {
            SpotListContent(viewModel: viewModel)
                .safeAreaInset(edge: .top) { SyncStatusBanner(repository: repository) }
                .refreshable { await viewModel.refresh() }
                .task { await viewModel.refresh() }
                .task(id: scenePhase) {
                    guard scenePhase == .active else { return }
                    for await update in live.updates() {
                        await viewModel.apply(update)
                    }
                    await viewModel.apply(.disconnected)
                }
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Label(viewModel.isLive ? "Live" : "Offline", systemImage: "circle.fill")
                            .labelStyle(.titleAndIcon)
                            .font(.caption)
                            .foregroundStyle(viewModel.isLive ? .green : .secondary)
                    }
                }
                .navigationTitle("UniEats")
                .searchable(
                    text: Binding(get: { viewModel.searchQuery }, set: viewModel.onQueryChange),
                    prompt: "Search spots"
                )
                .navigationDestination(for: String.self) { spotId in
                    SpotDetailView(spotId: spotId, repository: repository)
                }
        }
        .onOpenURL { url in
            if let spotId = DeepLink.spotId(from: url) {
                path = [spotId]
            }
        }
    }
}

private struct SpotListContent: View {
    let viewModel: SpotListViewModel
    @Query private var spots: [SpotEntity]

    init(viewModel: SpotListViewModel) {
        self.viewModel = viewModel
        let query = viewModel.searchQuery
        let category = viewModel.categoryFilter ?? ""
        _spots = Query(
            filter: #Predicate<SpotEntity> { spot in
                (query.isEmpty || spot.name.localizedStandardContains(query))
                    && (category.isEmpty || spot.category == category)
            },
            sort: \.name
        )
    }

    var body: some View {
        List {
            CategoryFilterView(
                selectedCategory: viewModel.categoryFilter,
                onCategoryChange: viewModel.onCategoryChange
            )
            .listRowInsets(EdgeInsets())
            .listRowSeparator(.hidden)

            ForEach(spots) { entity in
                NavigationLink(value: entity.id) {
                    SpotRow(
                        spot: entity.spot,
                        isFavourite: viewModel.favouriteIds.contains(entity.id),
                        isPending: entity.pendingSync,
                        onToggleFavourite: { viewModel.toggleFavourite(entity.id) }
                    )
                }
                .task {
                    if entity.id == spots.last?.id { await viewModel.loadMore() }
                }
            }

            footer
                .listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .overlay { emptyOrErrorState }
    }

    @ViewBuilder
    private var footer: some View {
        if viewModel.isLoadingMore {
            ProgressView()
                .frame(maxWidth: .infinity)
        } else if let message = viewModel.errorMessage, !spots.isEmpty {
            VStack(spacing: 8) {
                Text("Showing saved spots. \(message)")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Button("Retry") { Task { await viewModel.refresh() } }
                    .buttonStyle(.bordered)
            }
            .frame(maxWidth: .infinity)
        }
    }

    @ViewBuilder
    private var emptyOrErrorState: some View {
        if spots.isEmpty {
            if viewModel.isLoading {
                ProgressView("Loading spots…")
            } else if let message = viewModel.errorMessage {
                ContentUnavailableView {
                    Label("Couldn't load spots", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(message)
                } actions: {
                    Button("Retry") { Task { await viewModel.refresh() } }
                        .buttonStyle(.borderedProminent)
                }
            } else {
                ContentUnavailableView(
                    "No spots found",
                    systemImage: "fork.knife",
                    description: Text("Try another search or category.")
                )
            }
        }
    }
}
