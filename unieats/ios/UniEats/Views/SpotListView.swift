import SwiftData
import SwiftUI

struct SpotListView: View {
    let repository: SpotRepository
    let live: LiveUpdateService
    let session: SessionStore
    let favourites: FavouritesStore
    @Binding var pendingSpotId: String?
    @State private var viewModel: SpotListViewModel
    @State private var path: [String] = []
    @Namespace private var photoTransition
    @Environment(\.scenePhase) private var scenePhase

    init(
        repository: SpotRepository,
        live: LiveUpdateService,
        session: SessionStore,
        notifier: SpotChangeNotifier,
        favourites: FavouritesStore,
        pendingSpotId: Binding<String?>
    ) {
        self.repository = repository
        self.live = live
        self.session = session
        self.favourites = favourites
        _pendingSpotId = pendingSpotId
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
                    ToolbarItem(placement: .topBarLeading) {
                        accountMenu
                    }
                    ToolbarItem(placement: .topBarTrailing) {
                        Label(viewModel.isLive ? "Live" : "Offline", systemImage: "circle.fill")
                            .labelStyle(.titleAndIcon)
                            .font(.caption)
                            .foregroundStyle(viewModel.isLive ? Color.openText : .secondary)
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
        .onChange(of: pendingSpotId, initial: true) { _, spotId in
            guard let spotId else { return }
            path = [spotId]
            pendingSpotId = nil
        }
    }

    private var accountMenu: some View {
        Menu("Account", systemImage: "person.crop.circle") {
            if session.biometrics.isAvailable {
                Toggle(
                    "Unlock with \(session.biometrics.kindName)",
                    isOn: Binding(
                        get: { session.biometricUnlockEnabled },
                        set: { on in Task { await session.setBiometricUnlock(on) } }
                    )
                )
            }
            Button("Log out", role: .destructive, action: repository.logOut)
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
                (query.isEmpty
                    || spot.name.localizedStandardContains(query)
                    || spot.spotDescription.localizedStandardContains(query))
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
