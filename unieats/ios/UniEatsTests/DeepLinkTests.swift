import Foundation
import Testing
@testable import UniEats

@Suite("DeepLink.spotId(from:)")
struct DeepLinkTests {
    @Test("accepts unieats://spots/<id> and https://unieats.app/spots/<id>", arguments: [
        "unieats://spots/spot-3",
        "https://unieats.app/spots/spot-3",
    ])
    func acceptsBothForms(_ link: String) {
        #expect(DeepLink.spotId(from: URL(string: link)!) == "spot-3")
    }

    @Test("rejects other hosts, paths and schemes", arguments: [
        "https://example.com/spots/spot-3",
        "unieats://reviews/spot-3",
        "unieats://spots/",
        "http://unieats.app/spots/spot-3",
    ])
    func rejectsOtherLinks(_ link: String) {
        #expect(DeepLink.spotId(from: URL(string: link)!) == nil)
    }
}
