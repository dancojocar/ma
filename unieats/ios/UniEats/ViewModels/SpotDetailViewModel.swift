import Foundation
import UniEatsDomain

@MainActor
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

    func addReview(stars: Int, text: String) async -> String? {
        do {
            try await repository.addReview(spotId: spotId, stars: stars, text: text)
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
            defer { isDescribing = false }
            do {
                for try await chunk in repository.describe(spot) {
                    dishDescription += chunk
                }
            } catch is CancellationError {
            } catch ApiError.unauthorized {
                repository.handleUnauthorized()
            } catch ApiError.noConnectivity {
                describeError = "Server unreachable. Is it running (npm start in unieats/server)?"
            } catch {
                describeError = error.localizedDescription
            }
        }
    }

    func cancelDescribe() {
        describeTask?.cancel()
    }

    func save(name: String, description: String, openNow: Bool) {
        do {
            try repository.editSpot(id: spotId, name: name, description: description, openNow: openNow)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
