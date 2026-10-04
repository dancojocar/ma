import Foundation

@MainActor
@Observable
final class SpotDetailViewModel {
    let spotId: String
    private(set) var spot: Spot?
    private(set) var reviews: [Review] = []
    private(set) var isLoading = false
    private(set) var errorMessage: String?

    private let api: ApiClient

    init(spotId: String, api: ApiClient) {
        self.spotId = spotId
        self.api = api
    }

    func load() async {
        isLoading = true
        defer { isLoading = false }
        errorMessage = nil
        do {
            async let spotDTO = api.getSpot(id: spotId)
            async let fetchedReviews = api.getReviews(spotId: spotId)
            spot = Spot(dto: try await spotDTO)
            reviews = try await fetchedReviews.sorted { $0.createdAt > $1.createdAt }
        } catch is CancellationError {
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
