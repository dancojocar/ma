import Foundation
import SwiftData

@Model
final class SpotEntity {
    @Attribute(.unique) var id: String
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
    var pendingSync: Bool

    init(dto: SpotDTO) {
        id = dto.id
        name = dto.name
        category = dto.category
        rating = dto.rating
        priceLevel = dto.priceLevel
        lat = dto.lat
        lng = dto.lng
        openNow = dto.openNow
        photoUrl = dto.photoUrl
        spotDescription = dto.description
        updatedAt = dto.updatedAt
        pendingSync = false
    }

    func update(from dto: SpotDTO) {
        name = dto.name
        category = dto.category
        rating = dto.rating
        priceLevel = dto.priceLevel
        lat = dto.lat
        lng = dto.lng
        openNow = dto.openNow
        photoUrl = dto.photoUrl
        spotDescription = dto.description
        updatedAt = dto.updatedAt
    }

    var spot: Spot {
        Spot(
            id: id, name: name, category: category, rating: rating, priceLevel: priceLevel,
            lat: lat, lng: lng, openNow: openNow, photoUrl: photoUrl,
            spotDescription: spotDescription, updatedAt: updatedAt
        )
    }
}
