import Foundation

@MainActor
@Observable
final class RemoteConfig {
    static let showNewRatingUIKey = "show_new_rating_ui"

    private(set) var showNewRatingUI = false
    private(set) var isFetching = false
    private(set) var lastFetched: Date?
    private(set) var errorMessage: String?

    private let api: ApiClient

    init(api: ApiClient) {
        self.api = api
    }

    func fetchAndActivate() async {
        isFetching = true
        defer { isFetching = false }
        errorMessage = nil
        do {
            let flags = try await api.getConfig().flags
            showNewRatingUI = flags[Self.showNewRatingUIKey] ?? false
            lastFetched = .now
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

struct RemoteConfigResponse: Decodable, Sendable {
    let flags: [String: Bool]
}
