import Foundation
import SwiftData
import Testing
import UniEatsDomain
@testable import UniEats

/// The parts of the UniEats server the sync tests need, with the same 409 rule
/// (`server/src/routes/spots.js`): a PATCH based on an older `updatedAt` is rejected with the
/// current spot.
final class SpotBackend {
    let server = StubServer()
    var spots: [String: SpotDTO] = [:]
    /// Replaces the normal PATCH handling, e.g. to hold or fail one request.
    var onPatch: ((StubServer.Request) async -> StubServer.Response)?
    /// Answers every request the backend does not handle itself.
    var otherRoutes: (StubServer.Request) async -> StubServer.Response = { _ in .status(404) }

    init(spots: [SpotDTO]) {
        for spot in spots { self.spots[spot.id] = spot }
        server.handler = { [weak self] request in await self?.handle(request) ?? .offline }
    }

    var patches: [StubServer.Request] {
        server.requests.filter { $0.method == "PATCH" }
    }

    func applyPatch(_ request: StubServer.Request) -> StubServer.Response {
        let id = request.path.replacing("spots/", with: "")
        guard let current = spots[id], let patch = try? request.decode(SpotPatch.self) else { return .status(404) }
        if patch.updatedAt < current.updatedAt {
            return .json(ConflictBody(error: .init(code: "conflict", message: "Spot was changed by someone else"), spot: current), status: 409)
        }
        let updated = SpotDTO(
            id: current.id, name: patch.name ?? current.name, category: current.category,
            rating: current.rating, priceLevel: current.priceLevel, lat: current.lat, lng: current.lng,
            openNow: patch.openNow ?? current.openNow, photoUrl: current.photoUrl,
            description: patch.description ?? current.description, updatedAt: current.updatedAt + 1_000
        )
        spots[id] = updated
        return .json(updated)
    }

    private func handle(_ request: StubServer.Request) async -> StubServer.Response {
        switch (request.method, request.path) {
        case ("POST", "auth/login"):
            guard let body = try? request.decode([String: String].self),
                  let email = body["email"], body["password"] == "password" else {
                return .status(401)
            }
            let userId = "user-" + email.prefix { $0 != "@" }
            return .json(LoginBody(token: "token-\(userId)", user: User(id: userId, email: email, displayName: userId)))
        case ("GET", let path) where path.hasPrefix("spots/"):
            guard let spot = spots[path.replacing("spots/", with: "")] else { return .status(404) }
            return .json(spot)
        case ("PATCH", _):
            if let onPatch { return await onPatch(request) }
            return applyPatch(request)
        default:
            return await otherRoutes(request)
        }
    }

    struct ConflictBody: Encodable {
        struct Message: Encodable { let code: String; let message: String }
        let error: Message
        let spot: SpotDTO
    }

    private struct LoginBody: Encodable {
        let token: String
        let user: User
    }
}

func spotDTO(_ id: String, name: String, updatedAt: Double = 1_700_000_000_000) -> SpotDTO {
    SpotDTO(
        id: id, name: name, category: "canteen", rating: 3.8, priceLevel: 1,
        lat: 44.4268, lng: 26.1025, openNow: true, photoUrl: "", description: "",
        updatedAt: updatedAt
    )
}

/// A repository on an in-memory store, talking to `backend`, with an in-memory token store.
struct SyncHarness {
    let backend: SpotBackend
    let container: ModelContainer
    let tokens = InMemoryTokenStore()
    let session: SessionStore
    let repository: SpotRepository

    init(spots: [SpotDTO] = [spotDTO("spot-1", name: "Central Canteen")]) throws {
        backend = SpotBackend(spots: spots)
        container = try ModelContainer(
            for: SpotEntity.self, ReviewEntity.self, OutboxEntry.self,
            configurations: ModelConfiguration(isStoredInMemoryOnly: true)
        )
        Self.containers.append(container)
        let api = ApiClient(baseURL: backend.server.baseURL, configuration: backend.server.configuration)
        session = SessionStore(api: api, tokens: tokens, defaults: isolatedDefaults(), biometrics: FakeBiometrics())
        repository = SpotRepository(
            context: container.mainContext, api: api, session: session,
            ai: AiDescribeClient(baseURL: backend.server.baseURL, session: backend.server.session)
        )
        for spot in spots { repository.apply(.spotCreated(Spot(dto: spot))) }
    }

    var context: ModelContext { container.mainContext }

    /// A `ModelContext` does not keep its container alive, and a task the repository starts on its
    /// own (the sync after an edit) can outlive the test that caused it.
    private static var containers: [ModelContainer] = []

    func logIn(_ email: String = "student@unieats.app") async throws {
        await session.login(email: email, password: "password")
        try #require(session.isLoggedIn)
    }

    func row(_ id: String = "spot-1") throws -> SpotEntity {
        try #require(find(id))
    }

    func find(_ id: String = "spot-1") -> SpotEntity? {
        try? context.fetch(FetchDescriptor<SpotEntity>(predicate: #Predicate { $0.id == id })).first
    }

    func outbox() throws -> [OutboxEntry] {
        try context.fetch(FetchDescriptor<OutboxEntry>(sortBy: [SortDescriptor(\.createdAt)]))
    }
}

/// Polls until `condition` holds, for work the code under test starts in its own tasks (the sync
/// after an edit, the refresh after a discard).
func eventually(within timeout: Duration = .seconds(2), _ condition: () throws -> Bool) async rethrows -> Bool {
    let deadline = ContinuousClock.now + timeout
    while ContinuousClock.now < deadline {
        if try condition() { return true }
        try? await Task.sleep(for: .milliseconds(10))
    }
    return try condition()
}
