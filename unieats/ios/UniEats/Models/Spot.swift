import Foundation

struct Spot: Identifiable, Hashable, Sendable {
    let id: String
    var name: String
    var category: String
    var rating: Double
    var priceLevel: Int
    var lat: Double
    var lng: Double
    var openNow: Bool
    var photoUrl: String
    var spotDescription: String
    var updatedAt: Double
}

struct SpotDTO: Codable, Sendable {
    let id: String
    let name: String
    let category: String
    let rating: Double
    let priceLevel: Int
    let lat: Double
    let lng: Double
    let openNow: Bool
    let photoUrl: String
    let description: String
    let updatedAt: Double
}

extension Spot {
    init(dto: SpotDTO) {
        self.init(
            id: dto.id, name: dto.name, category: dto.category, rating: dto.rating,
            priceLevel: dto.priceLevel, lat: dto.lat, lng: dto.lng, openNow: dto.openNow,
            photoUrl: dto.photoUrl, spotDescription: dto.description, updatedAt: dto.updatedAt
        )
    }
}

struct SpotsPage: Decodable, Sendable {
    let spots: [SpotDTO]
    let page: Int
    let hasNextPage: Bool
}
