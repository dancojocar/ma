public struct Spot: Identifiable, Hashable, Sendable {
    public let id: String
    public var name: String
    public var category: String
    public var rating: Double
    public var priceLevel: Int
    public var lat: Double
    public var lng: Double
    public var openNow: Bool
    public var photoUrl: String
    public var description: String
    public var updatedAt: Double

    public init(
        id: String, name: String, category: String, rating: Double, priceLevel: Int,
        lat: Double, lng: Double, openNow: Bool, photoUrl: String, description: String,
        updatedAt: Double
    ) {
        self.id = id
        self.name = name
        self.category = category
        self.rating = rating
        self.priceLevel = priceLevel
        self.lat = lat
        self.lng = lng
        self.openNow = openNow
        self.photoUrl = photoUrl
        self.description = description
        self.updatedAt = updatedAt
    }
}
