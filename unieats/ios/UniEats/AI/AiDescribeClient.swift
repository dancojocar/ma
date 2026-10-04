import Foundation
import UniEatsDomain

enum AiDescribeError: LocalizedError {
    case rateLimited
    case generationFailed(String)

    var errorDescription: String? {
        switch self {
        case .rateLimited: "Too many requests — wait a minute and try again."
        case .generationFailed(let message): message
        }
    }
}

struct AiDescribeClient: Sendable {
    private let baseURL: URL
    private let session: URLSession

    init(baseURL: URL = ApiConfig.baseURL, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.session = session
    }

    /// Streams the description text chunk by chunk from the server's SSE response
    /// (`data: {"delta": …}` frames, then `event: done` or `event: error`).
    func describe(_ spot: Spot, token: String) -> AsyncThrowingStream<String, Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do {
                    let bytes = try await open(spot, token: token)
                    var event = "message"
                    for try await line in bytes.lines {
                        if line.hasPrefix("event:") {
                            event = line.dropFirst("event:".count).trimmingCharacters(in: .whitespaces)
                            continue
                        }
                        guard line.hasPrefix("data:") else { continue }
                        let data = Data(line.dropFirst("data:".count).utf8)
                        switch event {
                        case "done":
                            continuation.finish()
                            return
                        case "error":
                            let message = (try? JSONDecoder().decode(ErrorFrame.self, from: data))?.error.message
                            throw AiDescribeError.generationFailed(message ?? "The description could not be generated.")
                        default:
                            if let delta = try? JSONDecoder().decode(DeltaFrame.self, from: data).delta {
                                continuation.yield(delta)
                            }
                        }
                        event = "message"
                    }
                    continuation.finish()
                } catch let error as URLError where error.code == .cancelled {
                    continuation.finish(throwing: CancellationError())
                } catch is URLError {
                    continuation.finish(throwing: ApiError.noConnectivity)
                } catch {
                    continuation.finish(throwing: error)
                }
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }

    private func open(_ spot: Spot, token: String) async throws -> URLSession.AsyncBytes {
        var request = URLRequest(url: baseURL.appending(path: "ai/describe"))
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("text/event-stream", forHTTPHeaderField: "Accept")
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(DescribeBody(spot: spot.dto))

        let (bytes, response) = try await session.bytes(for: request)
        guard let http = response as? HTTPURLResponse else { throw ApiError.noConnectivity }
        switch http.statusCode {
        case 200..<300: return bytes
        case 401: throw ApiError.unauthorized
        case 429: throw AiDescribeError.rateLimited
        default: throw ApiError.http(statusCode: http.statusCode, message: "Describe failed")
        }
    }

    private struct DescribeBody: Encodable {
        let spot: SpotDTO
    }

    private struct DeltaFrame: Decodable {
        let delta: String
    }

    private struct ErrorFrame: Decodable {
        struct Body: Decodable { let message: String }
        let error: Body
    }
}
