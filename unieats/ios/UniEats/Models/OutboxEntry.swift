import Foundation
import SwiftData

@Model
final class OutboxEntry {
    @Attribute(.unique) var opId: String
    var type: String
    var entityId: String
    /// The user who made the edit; only their session replays it. The default lets SwiftData add
    /// the column to an outbox created before login existed.
    var userId: String = ""
    var payload: Data
    var createdAt: Date

    init(type: String, entityId: String, userId: String, payload: Data) {
        opId = UUID().uuidString
        self.type = type
        self.entityId = entityId
        self.userId = userId
        self.payload = payload
        createdAt = .now
    }
}

struct SpotPatch: Codable, Sendable {
    var name: String?
    var description: String?
    var openNow: Bool?
    /// The server version the user edited; the server answers 409 if it has a newer one.
    var updatedAt: Double
}
