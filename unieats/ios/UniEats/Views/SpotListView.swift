import SwiftUI

struct SpotListView: View {
    let api: ApiClient
    let live: LiveUpdateService
    @State private var viewModel: SpotListViewModel
    @State private var path: [String] = []
    @Environment(\.scenePhase) private var scenePhase

    init(api: ApiClient, live: LiveUpdateService) {
        self.api = api
        self.live = live
        _viewModel = State(initialValue: SpotListViewModel(api: api))
    }

    var body: some View {
        NavigationStack(path: $path) {
            List {
                CategoryFilterView(
                    selectedCategory: viewModel.categoryFilter,
                    onCategoryChange: viewModel.onCategoryChange
                )
                .listRowInsets(EdgeInsets())
                .listRowSeparator(.hidden)

                ForEach(viewModel.spots) { spot in
                    NavigationLink(value: spot.id) {
                        SpotRow(
                            spot: spot,
                            isFavourite: viewModel.favouriteIds.contains(spot.id),
                            onToggleFavourite: { viewModel.toggleFavourite(spot.id) }
                        )
                    }
                    .task { await viewModel.loadMoreIfNeeded(after: spot) }
                }

                footer
                    .listRowSeparator(.hidden)
            }
            .listStyle(.plain)
            .overlay { emptyOrErrorState }
            .refreshable { await viewModel.refresh() }
            .task { await viewModel.refresh() }
            .task(id: scenePhase) {
                guard scenePhase == .active else { return }
                for await update in live.updates() {
                    viewModel.apply(update)
                }
                viewModel.apply(.disconnected)
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
                SpotDetailView(spotId: spotId, api: api)
            }
        }
        .onOpenURL { url in
            if let spotId = DeepLink.spotId(from: url) {
                path = [spotId]
            }
        }
    }

    @ViewBuilder
    private var footer: some View {
        if viewModel.isLoadingMore {
            ProgressView()
                .frame(maxWidth: .infinity)
        } else if let message = viewModel.errorMessage, !viewModel.spots.isEmpty {
            VStack(spacing: 8) {
                Text(message)
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                Button("Retry") { Task { await viewModel.loadMore() } }
                    .buttonStyle(.bordered)
            }
            .frame(maxWidth: .infinity)
        }
    }

    @ViewBuilder
    private var emptyOrErrorState: some View {
        if viewModel.spots.isEmpty {
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
