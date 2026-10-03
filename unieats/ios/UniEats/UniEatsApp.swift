import SwiftData
import SwiftUI

@main
struct UniEatsApp: App {
    private let container: ModelContainer
    private let repository: SpotRepository
    private let live = LiveUpdateService()
    private let notifier = SpotChangeNotifier()
    @State private var session: SessionStore
    @State private var connectivity = ConnectivityMonitor()

    init() {
        do {
            container = try ModelContainer(for: SpotEntity.self, ReviewEntity.self, OutboxEntry.self)
        } catch {
            fatalError("Could not open the local database: \(error)")
        }
        let api = ApiClient()
        let session = SessionStore(api: api)
        _session = State(initialValue: session)
        repository = SpotRepository(context: container.mainContext, api: api, session: session)
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if session.isLoggedIn {
                    TabView {
                        Tab("Spots", systemImage: "fork.knife") {
                            SpotListView(repository: repository, live: live, session: session, notifier: notifier)
                        }
                        Tab("Near Me", systemImage: "location") {
                            NearMeView(repository: repository)
                        }
                    }
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
