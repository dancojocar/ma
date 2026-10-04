import Foundation
import SwiftData

@MainActor
@Observable
final class SpotRepository {
    private(set) var isSyncing = false
    private(set) var lastSyncError: String?

    private let context: ModelContext
    private let api: ApiClient
    private let session: SessionStore
    private var retryTask: Task<Void, Never>?
    private var retryAttempt = 0

    init(context: ModelContext, api: ApiClient, session: SessionStore) {
        self.context = context
        self.api = api
        self.session = session
    }

    // MARK: Reads: the server only feeds the local store; views observe it with @Query.

    func fetchPage(_ page: Int, query: String, category: String?) async throws -> SpotsPage {
        let result = try await api.listSpots(page: page, query: query, category: category)
        upsert(result.spots)
        return result
    }

    func fetchAllPages() async {
        var page = 1
        while let result = try? await fetchPage(page, query: "", category: nil), result.hasNextPage {
            page += 1
        }
    }

    func refreshSpot(id: String) async throws {
        upsert([try await api.getSpot(id: id)])
    }

    func refreshReviews(spotId: String) async throws {
        let reviews = try await api.getReviews(spotId: spotId)
        for review in reviews where self.review(id: review.id) == nil {
            context.insert(ReviewEntity(review: review))
        }
        try context.save()
    }

    func apply(_ update: LiveUpdate) {
        switch update {
        case .spotCreated(let spot), .spotUpdated(let spot):
            upsert([spot.dto])
        case .spotDeleted(let id):
            if let entity = spot(id: id), !entity.pendingSync {
                context.delete(entity)
                try? context.save()
            }
        case .connected, .disconnected:
            break
        }
    }

    // MARK: Writes: optimistic local change + outbox entry, then try to sync.

    func editSpot(id: String, name: String, description: String, openNow: Bool) throws {
        guard let entity = spot(id: id) else { return }
        let pending = outboxEntries().first { $0.entityId == id && $0.type == "update" }
        var patch = pending.flatMap { try? JSONDecoder().decode(SpotPatch.self, from: $0.payload) }
            ?? SpotPatch(updatedAt: entity.updatedAt)
        patch.name = name
        patch.description = description
        patch.openNow = openNow

        entity.name = name
        entity.spotDescription = description
        entity.openNow = openNow
        entity.pendingSync = true

        let payload = try JSONEncoder().encode(patch)
        if let pending {
            pending.payload = payload
        } else {
            context.insert(OutboxEntry(type: "update", entityId: id, payload: payload))
        }
        try context.save()
        Task { await sync() }
    }

    func addReview(spotId: String, stars: Int, text: String) async throws {
        guard let token = session.token else { throw ApiError.unauthorized }
        do {
            let review = try await api.createReview(
                spotId: spotId, stars: stars, text: text, idempotencyKey: UUID().uuidString, token: token
            )
            context.insert(ReviewEntity(review: review))
            try context.save()
        } catch ApiError.unauthorized {
            session.sessionExpired()
            throw ApiError.unauthorized
        }
    }

    func markSynced(_ entity: SpotEntity, server dto: SpotDTO) {
        entity.update(from: dto)
        entity.pendingSync = false
    }

    // MARK: Replay

    func sync() async {
        guard !isSyncing, let token = session.token else { return }
        isSyncing = true
        defer { isSyncing = false }
        lastSyncError = nil

        for entry in outboxEntries() {
            guard let patch = try? JSONDecoder().decode(SpotPatch.self, from: entry.payload) else {
                context.delete(entry)
                continue
            }
            do {
                let dto = try await api.patchSpot(
                    id: entry.entityId, patch: patch, idempotencyKey: entry.opId, token: token
                )
                if let entity = spot(id: entry.entityId) { markSynced(entity, server: dto) }
                context.delete(entry)
            } catch ApiError.unauthorized {
                session.sessionExpired()
                return
            } catch ApiError.conflict(let server) {
                if let entity = spot(id: entry.entityId) { markSynced(entity, server: server) }
                context.delete(entry)
            } catch ApiError.http(let status, let message) where (400..<500).contains(status) {
                lastSyncError = message
                if let entity = spot(id: entry.entityId) {
                    entity.pendingSync = false
                    if status == 404 { context.delete(entity) }
                }
                context.delete(entry)
            } catch {
                lastSyncError = error.localizedDescription
                scheduleRetry()
                return
            }
            try? context.save()
        }
        retryAttempt = 0
    }

    /// Covers the case NWPathMonitor cannot see: the device stays online but the server is down.
    private func scheduleRetry() {
        let delay = min(60, 5 << min(retryAttempt, 4))
        retryAttempt += 1
        retryTask?.cancel()
        retryTask = Task { [weak self] in
            try? await Task.sleep(for: .seconds(delay))
            guard !Task.isCancelled else { return }
            await self?.sync()
        }
    }

    // MARK: Helpers

    private func upsert(_ dtos: [SpotDTO]) {
        for dto in dtos {
            if let existing = spot(id: dto.id) {
                if !existing.pendingSync { existing.update(from: dto) }
            } else {
                context.insert(SpotEntity(dto: dto))
            }
        }
        try? context.save()
    }

    private func spot(id: String) -> SpotEntity? {
        try? context.fetch(FetchDescriptor<SpotEntity>(predicate: #Predicate { $0.id == id })).first
    }

    private func review(id: String) -> ReviewEntity? {
        try? context.fetch(FetchDescriptor<ReviewEntity>(predicate: #Predicate { $0.id == id })).first
    }

    private func outboxEntries() -> [OutboxEntry] {
        (try? context.fetch(FetchDescriptor<OutboxEntry>(sortBy: [SortDescriptor(\.createdAt)]))) ?? []
    }
}
