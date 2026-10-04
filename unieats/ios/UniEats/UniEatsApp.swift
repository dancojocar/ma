import SwiftUI

@main
struct UniEatsApp: App {
    private let api = ApiClient()

    var body: some Scene {
        WindowGroup {
            SpotListView(api: api)
        }
    }
}
