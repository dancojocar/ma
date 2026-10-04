import Foundation
import SwiftData
import Testing
import UniEatsDomain
@testable import UniEats

@MainActor
@Suite("SpotRepository upsert keeps local edits")
struct OfflineUpsertTests {
    private let container: ModelContainer
    private let repository: SpotRepository

    init() throws {
        container = try ModelContainer(
            for: SpotEntity.self, ReviewEntity.self, OutboxEntry.self,
            configurations: ModelConfiguration(isStoredInMemoryOnly: true)
        )
        let api = ApiClient(baseURL: URL(string: "http://127.0.0.1:9/api")!)
        repository = SpotRepository(
            context: container.mainContext, api: api, session: SessionStore(api: api, keychain: KeychainHelper())
        )
    }

    private func serverSpot(name: String) -> Spot {
        Spot(
            id: "spot-1", name: name, category: "canteen", rating: 3.8, priceLevel: 1,
            lat: 44.4268, lng: 26.1025, openNow: true, photoUrl: "", description: "",
            updatedAt: 1_700_000_000_000
        )
    }

    private func storedSpot() throws -> SpotEntity? {
        try container.mainContext.fetch(FetchDescriptor<SpotEntity>()).first
    }

    @Test("a live update inserts a new spot and updates a synced one")
    func liveUpdateUpsertsSyncedRow() throws {
        repository.apply(.spotCreated(serverSpot(name: "Central Canteen")))
        repository.apply(.spotUpdated(serverSpot(name: "Central Canteen (renovated)")))

        #expect(try container.mainContext.fetchCount(FetchDescriptor<SpotEntity>()) == 1)
        #expect(try storedSpot()?.name == "Central Canteen (renovated)")
    }

    @Test("a live update does not overwrite a row with pendingSync = true")
    func liveUpdateSkipsPendingRow() throws {
        repository.apply(.spotCreated(serverSpot(name: "Central Canteen")))
        try repository.editSpot(id: "spot-1", name: "My offline edit", description: "", openNow: false)

        repository.apply(.spotUpdated(serverSpot(name: "Server rename")))

        let row = try #require(try storedSpot())
        #expect(row.name == "My offline edit")
        #expect(row.pendingSync)
        #expect(try container.mainContext.fetchCount(FetchDescriptor<OutboxEntry>()) == 1)
    }

    @Test("two offline edits of one spot share one outbox entry based on the server version")
    func editsCoalesceIntoOneOutboxEntry() throws {
        repository.apply(.spotCreated(serverSpot(name: "Central Canteen")))
        try repository.editSpot(id: "spot-1", name: "First", description: "", openNow: true)
        try repository.editSpot(id: "spot-1", name: "Second", description: "", openNow: true)

        let entries = try container.mainContext.fetch(FetchDescriptor<OutboxEntry>())
        let patch = try JSONDecoder().decode(SpotPatch.self, from: try #require(entries.first).payload)
        #expect(entries.count == 1)
        #expect(patch.name == "Second")
        #expect(patch.updatedAt == 1_700_000_000_000)
    }
}
