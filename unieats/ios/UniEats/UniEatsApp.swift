import SwiftData
import SwiftUI

@main
struct UniEatsApp: App {
    private let container: ModelContainer
    @State private var repository: SpotRepository
    private let live = LiveUpdateService()
    @State private var notifier = SpotChangeNotifier()
    @State private var session: SessionStore
    @State private var remoteConfig: RemoteConfig
    @State private var crashConsent = CrashReportingConsent()
    @State private var connectivity = ConnectivityMonitor()
    @State private var favourites = FavouritesStore()
    @State private var router: AppRouter
    @Environment(\.scenePhase) private var scenePhase

    init() {
        container = Self.makeContainer()
        let api = ApiClient()
        let session = SessionStore(api: api)
        _session = State(initialValue: session)
        _router = State(initialValue: AppRouter(session: session))
        _remoteConfig = State(initialValue: RemoteConfig(api: api))
        _repository = State(initialValue: SpotRepository(context: container.mainContext, api: api, session: session))
    }

    private static func makeContainer() -> ModelContainer {
        let schema = Schema([SpotEntity.self, ReviewEntity.self, OutboxEntry.self])
        let configuration = ModelConfiguration(schema: schema)
        if let container = try? ModelContainer(for: schema, configurations: configuration) {
            return container
        }
        // The store is a cache of the server. A store written by an incompatible build (e.g. an
        // older checkout of this repo) cannot be migrated, so start from an empty one.
        let store = configuration.url
        for suffix in ["", "-shm", "-wal"] {
            let file = store.deletingLastPathComponent().appending(path: store.lastPathComponent + suffix)
            try? FileManager.default.removeItem(at: file)
        }
        do {
            return try ModelContainer(for: schema, configurations: configuration)
        } catch {
            fatalError("Could not open the local database: \(error)")
        }
    }

    var body: some Scene {
        WindowGroup {
            Group {
                switch session.phase {
                case .signedIn:
                    TabView(selection: $router.tab) {
                        Tab("Spots", systemImage: "fork.knife", value: AppTab.spots) {
                            SpotListView(
                                repository: repository,
                                live: live,
                                notifier: notifier,
                                favourites: favourites,
                                router: router
                            )
                        }
                        Tab("Near Me", systemImage: "location", value: AppTab.nearMe) {
                            NearMeView(repository: repository, favourites: favourites)
                        }
                        Tab("Settings", systemImage: "gearshape", value: AppTab.settings) {
                            SettingsView(
                                session: session,
                                remoteConfig: remoteConfig,
                                crashConsent: crashConsent,
                                onLogOut: repository.logOut
                            )
                        }
                    }
                    .environment(remoteConfig)
                    .task { await notifier.requestPermission() }
                case .locked:
                    UnlockView(session: session, onUsePassword: repository.logOut)
                case .signedOut:
                    LoginView(session: session)
                }
            }
            // Here rather than in SpotListView, which exists only while signed in.
            .onOpenURL(perform: router.open)
            .onChange(of: session.isLoggedIn) { _, loggedIn in
                if !loggedIn { router.sessionEnded() }
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
