import Foundation
import SwiftData
import Testing
@testable import UniEats

@Suite("Review submission and its Idempotency-Key")
struct ReviewSubmissionTests {
    @Test("the key stays the same until the stars or the text change")
    func draftKey() {
        var draft = ReviewDraft()
        let initial = draft.idempotencyKey

        draft.stars = 5
        draft.text = ""
        #expect(draft.idempotencyKey == initial)

        draft.stars = 4
        let afterStars = draft.idempotencyKey
        #expect(afterStars != initial)

        draft.text = "Great soup"
        #expect(draft.idempotencyKey != afterStars)
    }

    @Test("a retry after a failure resends the same key; edited content gets a new one")
    func retryReusesKey() async throws {
        let harness = try SyncHarness()
        try await harness.logIn()
        var failNext = true
        harness.backend.otherRoutes = { request in
            guard request.method == "POST", request.path == "spots/spot-1/reviews" else { return .status(404) }
            if failNext {
                failNext = false
                return .status(503)
            }
            guard let body = try? request.decode(ReviewBody.self) else { return .status(400) }
            return .json(Review(
                id: UUID().uuidString, spotId: "spot-1", author: "Student",
                stars: body.stars, text: body.text, createdAt: 1_700_000_000_000
            ), status: 201)
        }
        let viewModel = SpotDetailViewModel(spotId: "spot-1", repository: harness.repository)
        var draft = ReviewDraft()
        draft.stars = 4
        draft.text = "Good soup"

        #expect(await viewModel.addReview(draft) != nil)
        #expect(await viewModel.addReview(draft) == nil)
        draft.text = "Good soup, slow service"
        #expect(await viewModel.addReview(draft) == nil)

        let keys = harness.backend.server.requests("POST", "spots/spot-1/reviews").map { $0.headers["Idempotency-Key"] }
        try #require(keys.count == 3)
        #expect(keys[0] != nil)
        #expect(keys[0] == keys[1])
        #expect(keys[2] == draft.idempotencyKey)
        #expect(keys[2] != keys[1])
        #expect(try harness.context.fetchCount(FetchDescriptor<ReviewEntity>()) == 2)
    }

    private struct ReviewBody: Decodable {
        let stars: Int
        let text: String
    }
}
