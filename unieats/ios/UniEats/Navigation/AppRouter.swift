import Foundation

enum AppTab: Hashable {
    case spots
    case nearMe
    case settings
}

/// The selected tab and a deep link that has not been shown yet. A link opened while logged out
/// or locked is kept until login or unlock.
@Observable
final class AppRouter {
    var tab = AppTab.spots
    private(set) var pendingSpotId: String?

    private let session: SessionStore

    init(session: SessionStore) {
        self.session = session
    }

    func open(_ url: URL) {
        guard let spotId = DeepLink.spotId(from: url) else { return }
        pendingSpotId = spotId
        tab = .spots
    }

    /// The spot to push onto the Spots stack, at most once, and only in a signed-in session.
    func takePendingSpotId() -> String? {
        guard session.isLoggedIn, let spotId = pendingSpotId else { return nil }
        pendingSpotId = nil
        return spotId
    }

    func sessionEnded() {
        tab = .spots
    }
}
