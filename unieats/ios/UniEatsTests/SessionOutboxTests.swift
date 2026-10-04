import Foundation
import SwiftData
import Testing
@testable import UniEats

@Suite("Queued edits across log out, 401 and a different user")
struct SessionOutboxTests {
    private func harnessWithQueuedEdit(patchResponse: StubServer.Response) async throws -> SyncHarness {
        let harness = try SyncHarness()
        try await harness.logIn()
        harness.backend.onPatch = { _ in patchResponse }
        try harness.repository.editSpot(id: "spot-1", name: "My edit", description: "", openNow: true)
        #expect(await eventually { harness.backend.patches.count == 1 && !harness.repository.isSyncing })
        return harness
    }

    @Test("Log out discards the outbox and the optimistic rows, then reloads them")
    func explicitLogOutClearsOutbox() async throws {
        let harness = try await harnessWithQueuedEdit(patchResponse: .offline)
        #expect(try harness.outbox().count == 1)

        harness.repository.logOut()

        #expect(try harness.outbox().isEmpty)
        #expect(harness.session.phase == .signedOut)
        #expect(harness.tokens.token == nil)
        #expect(try await eventually {
            try harness.context.fetch(FetchDescriptor<SpotEntity>()).map(\.name) == ["Central Canteen"]
        })
        #expect(try !harness.row().pendingSync)
    }

    @Test("a 401 ends the session but keeps the edit, which the same user sends after logging in again")
    func unauthorizedKeepsOutbox() async throws {
        let harness = try await harnessWithQueuedEdit(patchResponse: .status(401))

        #expect(harness.session.phase == .signedOut)
        #expect(harness.session.errorMessage != nil)
        let entry = try #require(try harness.outbox().first)
        #expect(entry.userId == "user-student")
        #expect(try harness.row().name == "My edit")
        #expect(try harness.row().pendingSync)

        harness.backend.onPatch = nil
        try await harness.logIn()
        await harness.repository.sync()

        #expect(try harness.outbox().isEmpty)
        #expect(harness.backend.patches.count == 2)
        #expect(harness.backend.spots["spot-1"]?.name == "My edit")
        #expect(try !harness.row().pendingSync)
    }

    @Test("edits queued by another user are discarded, not sent, when someone else logs in")
    func otherUsersEditsAreDiscarded() async throws {
        let harness = try await harnessWithQueuedEdit(patchResponse: .status(401))
        harness.backend.onPatch = nil

        try await harness.logIn("other@unieats.app")
        await harness.repository.sync()

        #expect(try harness.outbox().isEmpty)
        #expect(harness.backend.patches.count == 1)
        #expect(harness.backend.spots["spot-1"]?.name == "Central Canteen")
        #expect(await eventually { harness.find()?.name == "Central Canteen" })
        #expect(try !harness.row().pendingSync)
    }

    @Test("editing needs a session")
    func editWithoutSessionThrows() throws {
        let harness = try SyncHarness()

        #expect(throws: ApiError.self) {
            try harness.repository.editSpot(id: "spot-1", name: "Anonymous", description: "", openNow: true)
        }
        #expect(try harness.outbox().isEmpty)
    }
}
