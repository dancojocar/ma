import SwiftData
import SwiftUI

@main
struct UniEatsApp: App {
    private let container: ModelContainer
    private let repository: SpotRepository
    private let live = LiveUpdateService()
    private let notifier = SpotChangeNotifier()
    @State private var session: SessionStore
    @State private var remoteConfig: RemoteConfig
    @State private var crashConsent = CrashReportingConsent()
    @State private var connectivity = ConnectivityMonitor()

    init() {
        container = Self.makeContainer()
        let api = ApiClient()
        let session = SessionStore(api: api)
        _session = State(initialValue: session)
        _remoteConfig = State(initialValue: RemoteConfig(api: api))
        repository = SpotRepository(context: container.mainContext, api: api, session: session)
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
                if session.isLoggedIn {
                    TabView {
                        Tab("Spots", systemImage: "fork.knife") {
                            SpotListView(repository: repository, live: live, notifier: notifier)
                        }
                        Tab("Near Me", systemImage: "location") {
                            NearMeView(repository: repository)
                        }
                        Tab("Settings", systemImage: "gearshape") {
                            SettingsView(session: session, remoteConfig: remoteConfig, crashConsent: crashConsent)
                        }
                    }
                    .environment(remoteConfig)
                    .task { await notifier.requestPermission() }
                } else {
                    LoginView(session: session)
                }
            }
            .task(id: session.isLoggedIn) {
                connectivity.start { [repository] in
                    Task { await repository.sync() }
                }
                await repository.sync()
            }
        }
        .modelContainer(container)
    }
}
