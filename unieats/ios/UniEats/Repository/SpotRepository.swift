import Foundation
import SwiftData

@Observable
final class SpotRepository {
    private(set) var isSyncing = false
    private(set) var lastSyncError: String?

    private let context: ModelContext
    private let api: ApiClient
    private var retryTask: Task<Void, Never>?
    private var retryAttempt = 0
    private var inFlightOpId: String?

    init(context: ModelContext, api: ApiClient) {
        self.context = context
        self.api = api
    }

    // MARK: Reads: the server only feeds the local store; views observe it with @Query.

    func fetchPage(_ page: Int, query: String, category: String?) async throws -> SpotsPage {
        let result = try await api.listSpots(page: page, query: query, category: category)
        upsert(result.spots)
        return result
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
        let pending = outboxEntries().first {
            $0.entityId == id && $0.type == "update" && $0.opId != inFlightOpId
        }
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

    func markSynced(_ entity: SpotEntity, server dto: SpotDTO) {
        entity.update(from: dto)
        entity.pendingSync = false
    }

    // MARK: Replay

    /// Re-reads the outbox after every entry, so edits made while a sync runs are sent in the same run.
    func sync() async {
        guard !isSyncing else { return }
        isSyncing = true
        defer { isSyncing = false }
        lastSyncError = nil

        var handled: Set<String> = []
        while let entry = outboxEntries().first(where: { !handled.contains($0.opId) }) {
            handled.insert(entry.opId)
            guard await replay(entry) else {
                scheduleRetry()
                return
            }
        }
        retryAttempt = 0
    }

    /// Returns false when the server could not be reached and the replay should stop.
    private func replay(_ entry: OutboxEntry) async -> Bool {
        guard let patch = try? JSONDecoder().decode(SpotPatch.self, from: entry.payload) else {
            context.delete(entry)
            try? context.save()
            return true
        }
        do {
            let dto = try await send(entry, patch: patch)
            settle(entry, server: dto)
        } catch ApiError.conflict(let server) {
            resolveConflict(entry, server: server)
        } catch ApiError.http(let status, let message) where (400..<500).contains(status) {
            lastSyncError = message
            await reject(entry, status: status)
        } catch {
            lastSyncError = error.localizedDescription
            return false
        }
        return true
    }

    private func send(_ entry: OutboxEntry, patch: SpotPatch) async throws -> SpotDTO {
        inFlightOpId = entry.opId
        defer { inFlightOpId = nil }
        return try await api.patchSpot(id: entry.entityId, patch: patch, idempotencyKey: entry.opId)
    }

    private func settle(_ entry: OutboxEntry, server dto: SpotDTO) {
        let newer = newerEntries(than: entry)
        if let entity = spot(id: entry.entityId) {
            if newer.isEmpty {
                markSynced(entity, server: dto)
            } else {
                entity.updatedAt = dto.updatedAt
                rebase(newer, onto: dto.updatedAt)
            }
        }
        context.delete(entry)
        try? context.save()
    }

    /// The server only answers 409 when its copy is newer than the version the user edited. Later
    /// queued edits of the spot were based on that same stale version, so they are dropped too.
    private func resolveConflict(_ entry: OutboxEntry, server: SpotDTO) {
        if let entity = spot(id: entry.entityId) { markSynced(entity, server: server) }
        ([entry] + newerEntries(than: entry)).forEach(context.delete)
        try? context.save()
    }

    private func reject(_ entry: OutboxEntry, status: Int) async {
        let id = entry.entityId
        let hasNewer = !newerEntries(than: entry).isEmpty
        context.delete(entry)
        if let entity = spot(id: id) {
            if status == 404 {
                context.delete(entity)
            } else if !hasNewer {
                entity.pendingSync = false
            }
        }
        try? context.save()
        if status != 404, !hasNewer {
            try? await refreshSpot(id: id)
        }
    }

    private func rebase(_ entries: [OutboxEntry], onto updatedAt: Double) {
        for entry in entries {
            guard var patch = try? JSONDecoder().decode(SpotPatch.self, from: entry.payload) else { continue }
            patch.updatedAt = updatedAt
            if let payload = try? JSONEncoder().encode(patch) { entry.payload = payload }
        }
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

    private func newerEntries(than entry: OutboxEntry) -> [OutboxEntry] {
        outboxEntries().filter { $0.entityId == entry.entityId && $0.opId != entry.opId }
    }

    private func outboxEntries() -> [OutboxEntry] {
        (try? context.fetch(FetchDescriptor<OutboxEntry>(sortBy: [SortDescriptor(\.createdAt)]))) ?? []
    }
}
