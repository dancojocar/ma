import Foundation

struct Review: Identifiable, Codable, Sendable {
    let id: String
    let spotId: String
    let author: String
    let stars: Int
    let text: String
    let createdAt: Double
}
