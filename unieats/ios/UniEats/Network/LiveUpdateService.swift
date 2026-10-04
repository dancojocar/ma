import Foundation
import UniEatsDomain

enum LiveUpdate: Sendable, Equatable {
    case connected
    case disconnected
    case spotCreated(Spot)
    case spotUpdated(Spot)
    case spotDeleted(id: String)
}

struct LiveUpdateService: Sendable {
    private let url: URL
    private let session: URLSession

    init(url: URL = ApiConfig.liveURL, session: URLSession = .shared) {
        self.url = url
        self.session = session
    }

    /// Connects when iterated, reconnects with exponential backoff (1 s → 30 s), and closes the
    /// socket when the consuming task is cancelled (e.g. SwiftUI cancels `.task` on disappear).
    func updates() -> AsyncStream<LiveUpdate> {
        AsyncStream { continuation in
            let worker = Task {
                var attempt = 0
                while !Task.isCancelled {
                    let socket = session.webSocketTask(with: url)
                    socket.resume()
                    await withTaskCancellationHandler {
                        do {
                            try await Self.ping(socket)
                            attempt = 0
                            continuation.yield(.connected)
                            while true {
                                let message = try await socket.receive()
                                if let update = Self.decode(message) {
                                    continuation.yield(update)
                                }
                            }
                        } catch {
                            continuation.yield(.disconnected)
                        }
                    } onCancel: {
                        socket.cancel(with: .goingAway, reason: nil)
                    }
                    let delay = min(30, 1 << min(attempt, 5))
                    attempt += 1
                    try? await Task.sleep(for: .seconds(delay))
                }
                continuation.finish()
            }
            continuation.onTermination = { _ in worker.cancel() }
        }
    }

    private static func ping(_ socket: URLSessionWebSocketTask) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            socket.sendPing { error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume()
                }
            }
        }
    }

    private static func decode(_ message: URLSessionWebSocketTask.Message) -> LiveUpdate? {
        let data: Data
        switch message {
        case .string(let text): data = Data(text.utf8)
        case .data(let bytes): data = bytes
        @unknown default: return nil
        }
        guard let envelope = try? JSONDecoder().decode(Envelope.self, from: data) else { return nil }
        switch envelope.type {
        case "spot.created": return envelope.spot.map { .spotCreated(Spot(dto: $0)) }
        case "spot.updated": return envelope.spot.map { .spotUpdated(Spot(dto: $0)) }
        case "spot.deleted": return (envelope.id ?? envelope.spot?.id).map { .spotDeleted(id: $0) }
        default: return nil
        }
    }

    private struct Envelope: Decodable {
        let type: String
        let spot: SpotDTO?
        let id: String?
    }
}
