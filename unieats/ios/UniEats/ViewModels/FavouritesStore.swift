import Foundation

@MainActor
@Observable
final class FavouritesStore {
    private(set) var ids: Set<String> = []

    func contains(_ spotId: String) -> Bool {
        ids.contains(spotId)
    }

    func toggle(_ spotId: String) {
        if ids.contains(spotId) {
            ids.remove(spotId)
        } else {
            ids.insert(spotId)
        }
    }
}
