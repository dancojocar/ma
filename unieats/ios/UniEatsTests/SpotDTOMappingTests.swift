import Foundation
import Testing
import UniEatsDomain
@testable import UniEats

@Suite("SpotDTO mapping")
struct SpotDTOMappingTests {
    private let json = """
    {"id":"spot-3","name":"Pizza Stop","category":"fastfood","rating":4.1,"priceLevel":2,
     "lat":44.426,"lng":26.1015,"openNow":true,"photoUrl":"https://picsum.photos/seed/spot-3/400/300",
     "description":"Pizza by the slice for students on the go.","updatedAt":1700000002000}
    """

    @Test("decodes the CONTRACT JSON and maps every field to the domain Spot")
    func decodesContractJSON() throws {
        let dto = try JSONDecoder().decode(SpotDTO.self, from: Data(json.utf8))
        let spot = Spot(dto: dto)

        #expect(spot.id == "spot-3")
        #expect(spot.name == "Pizza Stop")
        #expect(spot.category == "fastfood")
        #expect(spot.rating == 4.1)
        #expect(spot.priceLevel == 2)
        #expect(spot.lat == 44.426)
        #expect(spot.lng == 26.1015)
        #expect(spot.openNow)
        #expect(spot.photoUrl == "https://picsum.photos/seed/spot-3/400/300")
        #expect(spot.description == "Pizza by the slice for students on the go.")
        #expect(spot.updatedAt == 1_700_000_002_000)
    }

    @Test("Spot → DTO → JSON keeps the CONTRACT field names")
    func encodesContractFieldNames() throws {
        let dto = try JSONDecoder().decode(SpotDTO.self, from: Data(json.utf8))
        let encoded = try JSONEncoder().encode(Spot(dto: dto).dto)
        let object = try #require(JSONSerialization.jsonObject(with: encoded) as? [String: Any])

        #expect(Set(object.keys) == [
            "id", "name", "category", "rating", "priceLevel", "lat", "lng",
            "openNow", "photoUrl", "description", "updatedAt",
        ])
    }

    @Test("an outbox PATCH payload omits unchanged fields but always carries updatedAt")
    func patchPayloadShape() throws {
        let patch = SpotPatch(name: "Pizza Stop (new oven)", updatedAt: 1_700_000_002_000)
        let object = try #require(
            JSONSerialization.jsonObject(with: JSONEncoder().encode(patch)) as? [String: Any]
        )

        #expect(Set(object.keys) == ["name", "updatedAt"])
    }
}
