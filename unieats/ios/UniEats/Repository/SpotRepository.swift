import BackgroundTasks
import Foundation
import OSLog
import SwiftData
import UniEatsDomain

@Observable
final class SpotRepository {
    static let backgroundSyncTaskId = "com.unieats.app.outbox-sync"

    private(set) var isSyncing = false
    private(set) var lastSyncError: String?

    private let context: ModelContext
    private let api: ApiClient
    private let session: SessionStore
    private let ai: AiDescribeClient
    private var retryTask: Task<Void, Never>?
    private var retryAttempt = 0
    private var inFlightOpId: String?

    init(
        context: ModelContext,
        api: ApiClient,
        session: SessionStore,
        ai: AiDescribeClient = AiDescribeClient(baseURL: ApiConfig.baseURL)
    ) {
        self.context = context
        self.api = api
        self.session = session
        self.ai = ai
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
        guard let userId = session.userId else { throw ApiError.unauthorized }
        guard let entity = spot(id: id) else { return }
        let pending = outboxEntries().first {
            $0.entityId == id && $0.type == "update" && $0.userId == userId && $0.opId != inFlightOpId
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
            context.insert(OutboxEntry(type: "update", entityId: id, userId: userId, payload: payload))
        }
        try context.save()
        Task { await sync() }
    }

    /// The caller keeps `idempotencyKey` for one submission, so a retry after a timeout or a 5xx
    /// cannot post the review twice.
    func addReview(spotId: String, stars: Int, text: String, idempotencyKey: String) async throws {
        guard let token = session.token else { throw ApiError.unauthorized }
        do {
            let review = try await api.createReview(
                spotId: spotId, stars: stars, text: text, idempotencyKey: idempotencyKey, token: token
            )
            context.insert(ReviewEntity(review: review))
            try context.save()
        } catch ApiError.unauthorized {
            session.sessionExpired()
            throw ApiError.unauthorized
        }
    }

    /// Sends the full spot (the server describes what it is given) with the session token. A 401
    /// ends the session like any other request, unless the user has logged in again meanwhile.
    func describe(_ spot: Spot, onChunk: (String) -> Void) async throws {
        guard let token = session.token else { throw ApiError.unauthorized }
        do {
            for try await chunk in ai.describe(spot.dto, token: token) {
                try Task.checkCancellation()
                onChunk(chunk)
            }
        } catch ApiError.unauthorized {
            if session.token == token { session.sessionExpired() }
            throw ApiError.unauthorized
        }
    }

    func markSynced(_ entity: SpotEntity, server dto: SpotDTO) {
        entity.update(from: dto)
        entity.pendingSync = false
    }

    // MARK: Session

    /// An explicit log out also drops every unsynced edit, so the next person to log in on this
    /// device never sends them. A 401 only ends the session (`SessionStore.sessionExpired()`).
    func logOut() {
        retryTask?.cancel()
        discardEdits { _ in true }
        session.logout()
    }

    // MARK: Replay

    /// Re-reads the outbox after every entry, so edits made while a sync runs are sent in the same run.
    func sync() async {
        guard let token = session.token, let userId = session.userId else { return }
        discardEdits { $0.userId != userId }
        guard !isSyncing else { return }
        isSyncing = true
        defer { isSyncing = false }
        lastSyncError = nil

        var handled: Set<String> = []
        while let entry = outboxEntries().first(where: { !handled.contains($0.opId) }) {
            handled.insert(entry.opId)
            switch await replay(entry, token: token) {
            case .next:
                continue
            case .retryLater:
                scheduleRetry()
                return
            case .stop:
                return
            }
        }
        retryAttempt = 0
    }

    func syncInBackground() async {
        await sync()
        scheduleBackgroundSync()
    }

    /// Asks the system for a `BGAppRefreshTask` while edits are still queued; iOS decides when (or
    /// whether) it runs.
    func scheduleBackgroundSync() {
        guard !outboxEntries().isEmpty else { return }
        let request = BGAppRefreshTaskRequest(identifier: Self.backgroundSyncTaskId)
        request.earliestBeginDate = .now + 15 * 60
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            // The simulator always refuses with BGTaskScheduler.Error.unavailable.
            Logger(subsystem: "com.unieats.app", category: "sync")
                .info("Background sync not scheduled: \(error.localizedDescription)")
        }
    }

    private enum ReplayResult {
        case next
        /// The server could not be reached.
        case retryLater
        /// The session ended.
        case stop
    }

    private func replay(_ entry: OutboxEntry, token: String) async -> ReplayResult {
        guard let patch = try? JSONDecoder().decode(SpotPatch.self, from: entry.payload) else {
            context.delete(entry)
            try? context.save()
            return .next
        }
        do {
            let dto = try await send(entry, patch: patch, token: token)
            settle(entry, server: dto)
        } catch is SessionChanged {
            return .stop
        } catch ApiError.unauthorized {
            session.sessionExpired()
            return .stop
        } catch ApiError.conflict(let server) {
            await resolveConflict(entry, patch: patch, server: server)
        } catch ApiError.http(let status, let message) where (400..<500).contains(status) {
            lastSyncError = message
            await reject(entry, status: status)
        } catch {
            lastSyncError = error.localizedDescription
            return .retryLater
        }
        return .next
    }

    private struct SessionChanged: Error {}

    /// Throws `SessionChanged` instead of the request's outcome when the session changed while the
    /// request was in flight: a log out has deleted the outbox, so `entry` must not be touched.
    private func send(_ entry: OutboxEntry, patch: SpotPatch, token: String) async throws -> SpotDTO {
        inFlightOpId = entry.opId
        defer { inFlightOpId = nil }
        let outcome: Result<SpotDTO, any Error>
        do {
            outcome = .success(
                try await api.patchSpot(id: entry.entityId, patch: patch, idempotencyKey: entry.opId, token: token)
            )
        } catch {
            outcome = .failure(error)
        }
        guard session.token == token else { throw SessionChanged() }
        return try outcome.get()
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

    /// The server only answers 409 when its copy is newer than the version the user edited, so the
    /// shared `SpotConflictResolver` picks the server copy. Later queued edits of the spot were based
    /// on that same stale version, so they are dropped too.
    private func resolveConflict(_ entry: OutboxEntry, patch: SpotPatch, server dto: SpotDTO) async {
        if let entity = spot(id: entry.entityId) {
            var edited = entity.spot
            edited.updatedAt = patch.updatedAt
            if SpotConflictResolver.winner(local: edited, server: Spot(dto: dto)) == .client {
                // Only a server that breaks CONTRACT §5 gets here. There is no client-wins resend,
                // so the edit is dropped and the row reloaded from the server.
                await reject(entry, status: 409)
                return
            }
            markSynced(entity, server: dto)
        }
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

    /// Deletes the matching outbox entries and the optimistic rows only they were covering, then
    /// reloads those rows from the server.
    private func discardEdits(where shouldDiscard: (OutboxEntry) -> Bool) {
        let entries = outboxEntries()
        let discarded = entries.filter(shouldDiscard)
        guard !discarded.isEmpty else { return }
        let keptSpotIds = Set(entries.filter { !shouldDiscard($0) }.map(\.entityId))
        let staleSpotIds = Set(discarded.map(\.entityId)).subtracting(keptSpotIds)
        discarded.forEach(context.delete)
        staleSpotIds.compactMap(spot(id:)).forEach(context.delete)
        try? context.save()
        Task {
            for id in staleSpotIds { try? await refreshSpot(id: id) }
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
