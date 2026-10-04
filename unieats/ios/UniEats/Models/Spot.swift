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

private func photo(_ id: String) -> String {
    "https://picsum.photos/seed/\(id)/400/300"
}

let sampleSpots: [Spot] = [
    Spot(id: "spot-1", name: "Central Canteen", category: "canteen", rating: 3.8, priceLevel: 1,
         lat: 44.4268, lng: 26.1025, openNow: true, photoUrl: photo("spot-1"),
         spotDescription: "The main campus canteen with daily hot meals.", updatedAt: 1_700_000_000_000),
    Spot(id: "spot-2", name: "Espresso Lab", category: "cafe", rating: 4.5, priceLevel: 2,
         lat: 44.4275, lng: 26.1030, openNow: true, photoUrl: photo("spot-2"),
         spotDescription: "Specialty coffee and pastries near the library.", updatedAt: 1_700_000_001_000),
    Spot(id: "spot-3", name: "Pizza Stop", category: "fastfood", rating: 4.1, priceLevel: 2,
         lat: 44.4260, lng: 26.1015, openNow: true, photoUrl: photo("spot-3"),
         spotDescription: "Pizza by the slice for students on the go.", updatedAt: 1_700_000_002_000),
    Spot(id: "spot-4", name: "Bread & Butter", category: "bakery", rating: 4.7, priceLevel: 1,
         lat: 44.4280, lng: 26.1040, openNow: false, photoUrl: photo("spot-4"),
         spotDescription: "Fresh bread and pastries baked every morning.", updatedAt: 1_700_000_003_000),
    Spot(id: "spot-5", name: "The Pub Garden", category: "bar", rating: 4.2, priceLevel: 3,
         lat: 44.4255, lng: 26.1010, openNow: false, photoUrl: photo("spot-5"),
         spotDescription: "Outdoor bar with craft beers and snacks.", updatedAt: 1_700_000_004_000),
    Spot(id: "spot-6", name: "Sushi Box", category: "fastfood", rating: 3.9, priceLevel: 2,
         lat: 44.4270, lng: 26.1050, openNow: true, photoUrl: photo("spot-6"),
         spotDescription: "Grab-and-go sushi rolls and bento boxes.", updatedAt: 1_700_000_005_000),
    Spot(id: "spot-7", name: "Campus Bistro", category: "cafe", rating: 4.3, priceLevel: 2,
         lat: 44.4265, lng: 26.1035, openNow: true, photoUrl: photo("spot-7"),
         spotDescription: "Relaxed cafe with sandwiches, salads and wifi.", updatedAt: 1_700_000_006_000),
    Spot(id: "spot-8", name: "Grandma's Kitchen", category: "canteen", rating: 4.6, priceLevel: 1,
         lat: 44.4272, lng: 26.1020, openNow: true, photoUrl: photo("spot-8"),
         spotDescription: "Traditional home-cooked Romanian meals.", updatedAt: 1_700_000_007_000),
]
