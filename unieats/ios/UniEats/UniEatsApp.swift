import SwiftData
import SwiftUI

enum AppTab: Hashable {
    case spots
    case nearMe
}

@main
struct UniEatsApp: App {
    private let container: ModelContainer
    @State private var repository: SpotRepository
    private let live = LiveUpdateService()
    @State private var notifier = SpotChangeNotifier()
    @State private var session: SessionStore
    @State private var connectivity = ConnectivityMonitor()
    @State private var favourites = FavouritesStore()
    @State private var pendingSpotId: String?
    @State private var tab = AppTab.spots
    @Environment(\.scenePhase) private var scenePhase

    init() {
        do {
            container = try ModelContainer(for: SpotEntity.self, ReviewEntity.self, OutboxEntry.self)
        } catch {
            fatalError("Could not open the local database: \(error)")
        }
        let api = ApiClient()
        let session = SessionStore(api: api)
        _session = State(initialValue: session)
        _repository = State(initialValue: SpotRepository(context: container.mainContext, api: api, session: session))
    }

    var body: some Scene {
        WindowGroup {
            Group {
                switch session.phase {
                case .signedIn:
                    TabView(selection: $tab) {
                        Tab("Spots", systemImage: "fork.knife", value: AppTab.spots) {
                            SpotListView(
                                repository: repository,
                                live: live,
                                session: session,
                                notifier: notifier,
                                favourites: favourites,
                                pendingSpotId: $pendingSpotId
                            )
                        }
                        Tab("Near Me", systemImage: "location", value: AppTab.nearMe) {
                            NearMeView(repository: repository, favourites: favourites)
                        }
                    }
                    .task { await notifier.requestPermission() }
                case .locked:
                    UnlockView(session: session, onUsePassword: repository.logOut)
                case .signedOut:
                    LoginView(session: session)
                }
            }
            // Here rather than in SpotListView, so a link opened while logged out or locked is
            // kept and opened after login or unlock.
            .onOpenURL { url in
                guard let spotId = DeepLink.spotId(from: url) else { return }
                pendingSpotId = spotId
                tab = .spots
            }
            .task(id: session.isLoggedIn) {
                connectivity.start { [repository] in
                    Task { await repository.sync() }
                }
                await repository.sync()
            }
            .onChange(of: scenePhase) { _, phase in
                if phase == .background { repository.scheduleBackgroundSync() }
            }
        }
        .modelContainer(container)
        .backgroundTask(.appRefresh(SpotRepository.backgroundSyncTaskId)) { [repository] in
            await repository.syncInBackground()
        }
    }
}
