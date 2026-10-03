import Foundation
import UniEatsDomain

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
            photoUrl: dto.photoUrl, description: dto.description, updatedAt: dto.updatedAt
        )
    }
}

extension Spot {
    var dto: SpotDTO {
        SpotDTO(
            id: id, name: name, category: category, rating: rating, priceLevel: priceLevel,
            lat: lat, lng: lng, openNow: openNow, photoUrl: photoUrl,
            description: description, updatedAt: updatedAt
        )
    }
}

struct SpotsPage: Decodable, Sendable {
    let spots: [SpotDTO]
    let page: Int
    let hasNextPage: Bool
}
