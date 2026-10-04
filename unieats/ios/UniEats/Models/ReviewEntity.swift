import Foundation
import SwiftData

@Model
final class ReviewEntity {
    @Attribute(.unique) var id: String
    var spotId: String
    var author: String
    var stars: Int
    var text: String
    var createdAt: Double

    init(review: Review) {
        id = review.id
        spotId = review.spotId
        author = review.author
        stars = review.stars
        text = review.text
        createdAt = review.createdAt
    }
}

extension Review {
    init(entity: ReviewEntity) {
        self.init(
            id: entity.id, spotId: entity.spotId, author: entity.author,
            stars: entity.stars, text: entity.text, createdAt: entity.createdAt
        )
    }
}
