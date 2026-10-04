import SwiftUI

@main
struct UniEatsApp: App {
    private let api = ApiClient()
    private let live = LiveUpdateService()

    var body: some Scene {
        WindowGroup {
            SpotListView(api: api, live: live)
        }
    }
}
