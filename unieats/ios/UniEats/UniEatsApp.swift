import SwiftData
import SwiftUI

@main
struct UniEatsApp: App {
    private let container: ModelContainer
    private let repository: SpotRepository
    private let live = LiveUpdateService()
    @State private var connectivity = ConnectivityMonitor()

    init() {
        do {
            container = try ModelContainer(for: SpotEntity.self, ReviewEntity.self, OutboxEntry.self)
        } catch {
            fatalError("Could not open the local database: \(error)")
        }
        repository = SpotRepository(context: container.mainContext, api: ApiClient())
    }

    var body: some Scene {
        WindowGroup {
            SpotListView(repository: repository, live: live)
                .task {
                    connectivity.start { [repository] in
                        Task { await repository.sync() }
                    }
                    await repository.sync()
                }
        }
        .modelContainer(container)
    }
}
