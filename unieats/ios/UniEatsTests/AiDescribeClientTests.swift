import Foundation
import Testing
import UniEatsDomain
@testable import UniEats

@Suite("Describe this dish: SSE parsing")
struct AiDescribeClientTests {
    private let server = StubServer()
    private let spot = spotDTO("spot-1", name: "Central Canteen")

    private var client: AiDescribeClient {
        AiDescribeClient(baseURL: server.baseURL, session: server.session)
    }

    private func respond(status: Int = 200, sse: String) {
        server.handler = { _ in .status(status, body: Data(sse.utf8), contentType: "text/event-stream") }
    }

    private func collect() async throws -> [String] {
        var chunks: [String] = []
        for try await chunk in client.describe(spot, token: "token-1") { chunks.append(chunk) }
        return chunks
    }

    @Test("yields every data frame's delta in order and stops at event: done")
    func streamsDeltas() async throws {
        respond(sse: """
        : comment lines and unknown fields are ignored
        data: {"delta":"Hearty "}

        data: {"delta":"soup, "}

        retry: 1000
        data: {"delta":"fresh bread."}

        event: done
        data: {}

        data: {"delta":"after done"}

        """)

        #expect(try await collect() == ["Hearty ", "soup, ", "fresh bread."])
    }

    @Test("handles CRLF line endings")
    func crlf() async throws {
        respond(sse: "data: {\"delta\":\"one \"}\r\n\r\ndata: {\"delta\":\"two\"}\r\n\r\nevent: done\r\ndata: {}\r\n\r\n")

        #expect(try await collect() == ["one ", "two"])
    }

    @Test("an event: error frame throws the server's message after the chunks before it")
    func errorFrame() async throws {
        respond(sse: """
        data: {"delta":"Half a "}

        event: error
        data: {"error":{"code":"upstream","message":"The model is overloaded"}}

        """)

        var chunks: [String] = []
        let error = await #expect(throws: AiDescribeError.self) {
            for try await chunk in client.describe(spot, token: "token-1") { chunks.append(chunk) }
        }
        #expect(error?.errorDescription == "The model is overloaded")
        #expect(chunks == ["Half a "])
    }

    @Test("a stream that ends without event: done finishes normally")
    func endsWithoutDone() async throws {
        respond(sse: "data: {\"delta\":\"cut short\"}\n\n")

        #expect(try await collect() == ["cut short"])
    }

    @Test("401 → unauthorized, 429 → rate limited, 500 → HTTP error")
    func statusCodes() async {
        respond(status: 401, sse: "")
        let unauthorized = await #expect(throws: ApiError.self) { try await collect() }
        #expect(unauthorized.map { if case .unauthorized = $0 { true } else { false } } == true)

        respond(status: 429, sse: "")
        let rateLimited = await #expect(throws: AiDescribeError.self) { try await collect() }
        #expect(rateLimited.map { if case .rateLimited = $0 { true } else { false } } == true)

        respond(status: 500, sse: "")
        let serverError = await #expect(throws: ApiError.self) { try await collect() }
        #expect(serverError.map { if case .http(500, _) = $0 { true } else { false } } == true)
    }

    @Test("sends the full spot with the Bearer token and asks for an event stream")
    func request() async throws {
        respond(sse: "event: done\ndata: {}\n\n")
        _ = try await collect()

        let request = try #require(server.requests("POST", "ai/describe").first)
        #expect(request.headers["Authorization"] == "Bearer token-1")
        #expect(request.headers["Accept"] == "text/event-stream")
        let body = try #require(try JSONSerialization.jsonObject(with: request.body) as? [String: Any])
        let sent = try #require(body["spot"] as? [String: Any])
        #expect(sent["id"] as? String == "spot-1")
        #expect(sent["openNow"] as? Bool == true)
    }

    @Test("SpotRepository.describe: a 401 ends the session")
    func repositoryUnauthorized() async throws {
        let harness = try SyncHarness()
        try await harness.logIn()
        harness.backend.otherRoutes = { _ in .status(401) }

        await #expect(throws: ApiError.self) {
            try await harness.repository.describe(Spot(dto: spot)) { _ in }
        }
        #expect(harness.session.phase == .signedOut)
        #expect(harness.session.errorMessage != nil)
    }
}
