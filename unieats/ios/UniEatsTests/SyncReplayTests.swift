import Foundation
import SwiftData
import Testing
@testable import UniEats

@Suite("Outbox replay")
struct SyncReplayTests {
    private let firstVersion = 1_700_000_000_000.0

    @Test("an edit made while the spot's PATCH is in flight is kept and sent rebased in the same run")
    func editDuringInFlightPatchIsRebased() async throws {
        let harness = try SyncHarness()
        try await harness.logIn()
        let gate = Gate()
        let backend = harness.backend
        backend.onPatch = { request in
            if backend.patches.count == 1 { await gate.pass() }
            return backend.applyPatch(request)
        }

        try harness.repository.editSpot(id: "spot-1", name: "First", description: "", openNow: true)
        await gate.waitForArrival()
        try harness.repository.editSpot(id: "spot-1", name: "Second", description: "", openNow: true)
        #expect(try harness.outbox().count == 2)
        gate.open()

        try #require(await eventually { backend.patches.count == 2 && !harness.repository.isSyncing })
        let first = try backend.patches[0].decode(SpotPatch.self)
        let second = try backend.patches[1].decode(SpotPatch.self)
        #expect(first.name == "First")
        #expect(first.updatedAt == firstVersion)
        #expect(second.name == "Second")
        #expect(second.updatedAt == firstVersion + 1_000)
        #expect(backend.patches[0].headers["Idempotency-Key"] != backend.patches[1].headers["Idempotency-Key"])

        let row = try harness.row()
        #expect(row.name == "Second")
        #expect(!row.pendingSync)
        #expect(row.updatedAt == firstVersion + 2_000)
        #expect(try harness.outbox().isEmpty)
        #expect(backend.spots["spot-1"]?.name == "Second")
    }

    @Test("edits queued while a sync runs are sent by that run")
    func syncLoopPicksUpEditsQueuedDuringRun() async throws {
        let harness = try SyncHarness(spots: [
            spotDTO("spot-1", name: "Central Canteen"), spotDTO("spot-2", name: "Pizza Stop"),
        ])
        try await harness.logIn()
        let gate = Gate()
        let backend = harness.backend
        backend.onPatch = { request in
            if backend.patches.count == 1 { await gate.pass() }
            return backend.applyPatch(request)
        }

        try harness.repository.editSpot(id: "spot-1", name: "Canteen (new menu)", description: "", openNow: true)
        await gate.waitForArrival()
        #expect(harness.repository.isSyncing)
        // Its own sync() returns at once because a sync is already running.
        try harness.repository.editSpot(id: "spot-2", name: "Pizza Stop (new oven)", description: "", openNow: true)
        gate.open()

        #expect(try await eventually { try harness.outbox().isEmpty && !harness.repository.isSyncing })
        #expect(backend.patches.map(\.path) == ["spots/spot-1", "spots/spot-2"])
        #expect(try !harness.row("spot-1").pendingSync)
        #expect(try harness.row("spot-2").name == "Pizza Stop (new oven)")
        #expect(try !harness.row("spot-2").pendingSync)
    }

    @Test("a 409 takes the server copy and drops the op and the later queued ops of that spot")
    func conflictServerWins() async throws {
        let harness = try SyncHarness(spots: [
            spotDTO("spot-1", name: "Central Canteen"), spotDTO("spot-2", name: "Pizza Stop"),
        ])
        try await harness.logIn()
        let backend = harness.backend
        backend.spots["spot-1"] = spotDTO("spot-1", name: "Renamed by someone else", updatedAt: firstVersion + 5_000)
        let gate = Gate()
        backend.onPatch = { request in
            if backend.patches.count == 1 { await gate.pass() }
            return backend.applyPatch(request)
        }

        try harness.repository.editSpot(id: "spot-1", name: "Stale edit", description: "", openNow: true)
        await gate.waitForArrival()
        try harness.repository.editSpot(id: "spot-1", name: "Another stale edit", description: "", openNow: true)
        try harness.repository.editSpot(id: "spot-2", name: "Pizza Stop (new oven)", description: "", openNow: true)
        #expect(try harness.outbox().count == 3)
        gate.open()

        #expect(try await eventually { try harness.outbox().isEmpty && !harness.repository.isSyncing })
        #expect(backend.patches.map(\.path) == ["spots/spot-1", "spots/spot-2"])
        let row = try harness.row("spot-1")
        #expect(row.name == "Renamed by someone else")
        #expect(row.updatedAt == firstVersion + 5_000)
        #expect(!row.pendingSync)
        #expect(try harness.row("spot-2").name == "Pizza Stop (new oven)")
    }

    @Test("a 409 the app cannot read or another 4xx drops the edit and reloads the spot", arguments: [
        (409, "{}"),
        (422, #"{"error":{"code":"validation","message":"name too long"}}"#),
        (403, #"{"error":{"code":"forbidden","message":"not yours"}}"#),
    ])
    func rejectedEditIsDroppedAndRefreshed(status: Int, body: String) async throws {
        let harness = try SyncHarness()
        try await harness.logIn()
        harness.backend.onPatch = { _ in .status(status, body: Data(body.utf8)) }

        try harness.repository.editSpot(id: "spot-1", name: "Rejected edit", description: "", openNow: false)

        #expect(await eventually { harness.find()?.name == "Central Canteen" })
        let row = try harness.row()
        #expect(!row.pendingSync)
        #expect(row.openNow)
        #expect(try harness.outbox().isEmpty)
        #expect(harness.repository.lastSyncError != nil)
        #expect(harness.backend.server.requests("GET", "spots/spot-1").count == 1)
    }

    @Test("a 404 removes the spot")
    func deletedSpotIsRemoved() async throws {
        let harness = try SyncHarness()
        try await harness.logIn()
        harness.backend.onPatch = { _ in .status(404) }

        try harness.repository.editSpot(id: "spot-1", name: "Edit of a deleted spot", description: "", openNow: true)

        #expect(try await eventually { try harness.outbox().isEmpty && !harness.repository.isSyncing })
        #expect(try harness.context.fetchCount(FetchDescriptor<SpotEntity>()) == 0)
    }

    @Test("without a connection the edit stays queued")
    func offlineKeepsEdit() async throws {
        let harness = try SyncHarness()
        try await harness.logIn()
        harness.backend.onPatch = { _ in .offline }

        try harness.repository.editSpot(id: "spot-1", name: "Offline edit", description: "", openNow: true)

        #expect(await eventually { harness.backend.patches.count == 1 && !harness.repository.isSyncing })
        #expect(try harness.outbox().count == 1)
        #expect(try harness.row().name == "Offline edit")
        #expect(try harness.row().pendingSync)
        #expect(harness.repository.lastSyncError != nil)
        harness.repository.logOut()
    }
}
