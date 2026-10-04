import Foundation

@MainActor
@Observable
final class SpotDetailViewModel {
    let spotId: String
    private(set) var isLoading = false
    private(set) var errorMessage: String?

    private let repository: SpotRepository

    init(spotId: String, repository: SpotRepository) {
        self.spotId = spotId
        self.repository = repository
    }

    func refresh() async {
        isLoading = true
        defer { isLoading = false }
        errorMessage = nil
        do {
            try await repository.refreshSpot(id: spotId)
            try await repository.refreshReviews(spotId: spotId)
        } catch is CancellationError {
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func addReview(stars: Int, text: String) async -> String? {
        do {
            try await repository.addReview(spotId: spotId, stars: stars, text: text)
            return nil
        } catch {
            return error.localizedDescription
        }
    }

    func save(name: String, description: String, openNow: Bool) {
        do {
            try repository.editSpot(id: spotId, name: name, description: description, openNow: openNow)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
