import SwiftData
import SwiftUI

struct SpotListView: View {
    let repository: SpotRepository
    let live: LiveUpdateService
    let favourites: FavouritesStore
    @State private var viewModel: SpotListViewModel
    @State private var path: [String] = []
    @Namespace private var photoTransition
    @Environment(\.scenePhase) private var scenePhase

    init(repository: SpotRepository, live: LiveUpdateService, notifier: SpotChangeNotifier, favourites: FavouritesStore) {
        self.repository = repository
        self.favourites = favourites
        self.live = live
        _viewModel = State(initialValue: SpotListViewModel(repository: repository, notifier: notifier))
    }

    var body: some View {
        NavigationStack(path: $path) {
            SpotListContent(viewModel: viewModel, favourites: favourites, photoTransition: photoTransition)
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
                    SpotDetailView(
                        spotId: spotId,
                        isFavourite: favourites.contains(spotId),
                        onToggleFavourite: { favourites.toggle(spotId) },
                        repository: repository
                    )
                        .navigationTransition(.zoom(sourceID: spotId, in: photoTransition))
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
    let favourites: FavouritesStore
    let photoTransition: Namespace.ID
    @Query private var spots: [SpotEntity]

    init(viewModel: SpotListViewModel, favourites: FavouritesStore, photoTransition: Namespace.ID) {
        self.viewModel = viewModel
        self.favourites = favourites
        self.photoTransition = photoTransition
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
                        isFavourite: favourites.contains(entity.id),
                        isPending: entity.pendingSync,
                        photoTransition: photoTransition,
                        onToggleFavourite: { favourites.toggle(entity.id) }
                    )
                }
                .transition(.move(edge: .leading).combined(with: .opacity))
                .task {
                    if entity.id == spots.last?.id { await viewModel.loadMore() }
                }
            }

            footer
                .listRowSeparator(.hidden)
        }
        .listStyle(.plain)
        .animation(.easeInOut(duration: 0.25), value: spots.map(\.id))
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
