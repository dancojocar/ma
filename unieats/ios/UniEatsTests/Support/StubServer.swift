import Foundation

/// An in-process HTTP server for `ApiClient` and `AiDescribeClient`. Every instance gets its own
/// host name, so suites running in parallel never see each other's requests.
final class StubServer {
    struct Request {
        let method: String
        let path: String
        let headers: [String: String]
        let body: Data

        func decode<T: Decodable>(_ type: T.Type) throws -> T {
            try JSONDecoder().decode(type, from: body)
        }
    }

    enum Response {
        case status(Int, body: Data = Data("{}".utf8), contentType: String = "application/json")
        case offline

        static func json(_ value: some Encodable, status: Int = 200) -> Response {
            .status(status, body: (try? JSONEncoder().encode(value)) ?? Data())
        }
    }

    let baseURL: URL
    private(set) var requests: [Request] = []
    var handler: (Request) async -> Response

    init(handler: @escaping (Request) async -> Response = { _ in .status(404) }) {
        let host = "stub-\(UUID().uuidString.lowercased()).test"
        baseURL = URL(string: "http://\(host)/api")!
        self.handler = handler
        Self.servers[host] = self
    }

    var configuration: URLSessionConfiguration {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.protocolClasses = [StubURLProtocol.self]
        return configuration
    }

    var session: URLSession { URLSession(configuration: configuration) }

    func requests(_ method: String, _ path: String) -> [Request] {
        requests.filter { $0.method == method && $0.path == path }
    }

    fileprivate func respond(to request: Request) async -> Response {
        requests.append(request)
        return await handler(request)
    }

    fileprivate static var servers: [String: StubServer] = [:]
}

nonisolated final class StubURLProtocol: URLProtocol, @unchecked Sendable {
    private var loading: Task<Void, Never>?

    override class func canInit(with request: URLRequest) -> Bool { true }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }

    override func startLoading() {
        let urlRequest = request
        let body = Self.body(of: urlRequest)
        loading = Task { @MainActor in
            guard let url = urlRequest.url, let host = url.host(), let server = StubServer.servers[host] else {
                return self.fail(URLError(.cannotFindHost))
            }
            let path = url.path().replacing(server.baseURL.path() + "/", with: "", maxReplacements: 1)
            let request = StubServer.Request(
                method: urlRequest.httpMethod ?? "GET",
                path: path,
                headers: urlRequest.allHTTPHeaderFields ?? [:],
                body: body
            )
            switch await server.respond(to: request) {
            case .offline:
                self.fail(URLError(.notConnectedToInternet))
            case .status(let status, let data, let contentType):
                let response = HTTPURLResponse(
                    url: url, statusCode: status, httpVersion: "HTTP/1.1",
                    headerFields: ["Content-Type": contentType]
                )!
                self.client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
                self.client?.urlProtocol(self, didLoad: data)
                self.client?.urlProtocolDidFinishLoading(self)
            }
        }
    }

    override func stopLoading() {
        loading?.cancel()
    }

    private func fail(_ error: URLError) {
        client?.urlProtocol(self, didFailWithError: error)
    }

    /// URLSession hands a custom protocol the body as a stream, not as `httpBody`.
    private static func body(of request: URLRequest) -> Data {
        if let body = request.httpBody { return body }
        guard let stream = request.httpBodyStream else { return Data() }
        stream.open()
        defer { stream.close() }
        var data = Data()
        var buffer = [UInt8](repeating: 0, count: 4096)
        while stream.hasBytesAvailable {
            let count = stream.read(&buffer, maxLength: buffer.count)
            guard count > 0 else { break }
            data.append(buffer, count: count)
        }
        return data
    }
}
