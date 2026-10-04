import SwiftData
import SwiftUI

@main
struct UniEatsApp: App {
    private let container: ModelContainer
    @State private var repository: SpotRepository
    private let live = LiveUpdateService()
    @State private var session: SessionStore
    @State private var connectivity = ConnectivityMonitor()
    @State private var pendingSpotId: String?

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
                    SpotListView(repository: repository, live: live, session: session, pendingSpotId: $pendingSpotId)
                case .locked:
                    UnlockView(session: session, onUsePassword: repository.logOut)
                case .signedOut:
                    LoginView(session: session)
                }
            }
            // Here rather than in SpotListView, so a link opened while logged out or locked is
            // kept and opened after login or unlock.
            .onOpenURL { url in
                pendingSpotId = DeepLink.spotId(from: url) ?? pendingSpotId
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
