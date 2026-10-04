import Foundation
import UniEatsDomain

@Observable
final class SpotDetailViewModel {
    let spotId: String
    private(set) var isLoading = false
    private(set) var errorMessage: String?
    private(set) var dishDescription = ""
    private(set) var isDescribing = false
    private(set) var describeError: String?

    private let repository: SpotRepository
    private var describeTask: Task<Void, Never>?

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

    func addReview(_ draft: ReviewDraft) async -> String? {
        do {
            try await repository.addReview(
                spotId: spotId, stars: draft.stars, text: draft.text, idempotencyKey: draft.idempotencyKey
            )
            return nil
        } catch {
            return error.localizedDescription
        }
    }

    func describe(_ spot: Spot) {
        describeTask?.cancel()
        dishDescription = ""
        describeError = nil
        isDescribing = true
        describeTask = Task {
            var failure: String?
            do {
                try await repository.describe(spot) { dishDescription += $0 }
            } catch {
                failure = error.localizedDescription
            }
            // A cancelled task must not touch the state a newer describe() has already reset.
            guard !Task.isCancelled else { return }
            describeError = failure
            isDescribing = false
        }
    }

    func cancelDescribe() {
        describeTask?.cancel()
        describeTask = nil
        isDescribing = false
    }

    func save(name: String, description: String, openNow: Bool) {
        do {
            try repository.editSpot(id: spotId, name: name, description: description, openNow: openNow)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
