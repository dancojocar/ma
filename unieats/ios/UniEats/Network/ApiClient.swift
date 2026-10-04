import Foundation

enum ApiConfig {
    static let baseURL = URL(string: ProcessInfo.processInfo.environment["UNIEATS_API_URL"] ?? "http://localhost:3000/api")!

    static var liveURL: URL {
        var components = URLComponents(url: baseURL, resolvingAgainstBaseURL: false)!
        components.scheme = components.scheme == "https" ? "wss" : "ws"
        components.path = "/live"
        return components.url!
    }
}

struct ApiClient: Sendable {
    static let pageSize = 20

    private let baseURL: URL
    private let session: URLSession

    init(baseURL: URL = ApiConfig.baseURL) {
        self.baseURL = baseURL
        let configuration = URLSessionConfiguration.default
        configuration.timeoutIntervalForRequest = 15
        configuration.timeoutIntervalForResource = 30
        configuration.waitsForConnectivity = false
        self.session = URLSession(configuration: configuration)
    }

    func listSpots(page: Int, query: String, category: String?) async throws -> SpotsPage {
        var items = [
            URLQueryItem(name: "page", value: String(page)),
            URLQueryItem(name: "limit", value: String(Self.pageSize)),
        ]
        if !query.isEmpty { items.append(URLQueryItem(name: "q", value: query)) }
        if let category { items.append(URLQueryItem(name: "category", value: category)) }
        return try await get("spots", query: items)
    }

    func getSpot(id: String) async throws -> SpotDTO {
        try await get("spots/\(id)")
    }

    func getReviews(spotId: String) async throws -> [Review] {
        try await get("spots/\(spotId)/reviews")
    }

    func getConfig() async throws -> RemoteConfigResponse {
        try await get("config")
    }

    func login(email: String, password: String) async throws -> LoginResponse {
        try await send(jsonRequest("POST", "auth/login", body: LoginBody(email: email, password: password)))
    }

    func patchSpot(id: String, patch: SpotPatch, idempotencyKey: String, token: String) async throws -> SpotDTO {
        var request = try jsonRequest("PATCH", "spots/\(id)", body: patch, token: token)
        request.setValue(idempotencyKey, forHTTPHeaderField: "Idempotency-Key")
        return try await send(request)
    }

    func createReview(spotId: String, stars: Int, text: String, idempotencyKey: String, token: String) async throws -> Review {
        var request = try jsonRequest("POST", "spots/\(spotId)/reviews", body: ReviewBody(stars: stars, text: text), token: token)
        request.setValue(idempotencyKey, forHTTPHeaderField: "Idempotency-Key")
        return try await send(request)
    }

    private func jsonRequest(_ method: String, _ path: String, body: some Encodable, token: String? = nil) throws -> URLRequest {
        var request = URLRequest(url: baseURL.appending(path: path))
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        request.httpBody = try JSONEncoder().encode(body)
        return request
    }

    private func get<T: Decodable>(_ path: String, query: [URLQueryItem] = []) async throws -> T {
        var url = baseURL.appending(path: path)
        if !query.isEmpty { url.append(queryItems: query) }
        var request = URLRequest(url: url)
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        return try await send(request)
    }

    private func send<T: Decodable>(_ request: URLRequest) async throws -> T {
        let data = try await perform(request)
        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            throw ApiError.decodingFailed(error)
        }
    }

    private func perform(_ request: URLRequest) async throws -> Data {
        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(for: request)
        } catch let error as URLError where error.code == .cancelled {
            throw CancellationError()
        } catch let error as URLError where error.code == .timedOut {
            throw ApiError.timeout
        } catch is URLError {
            throw ApiError.noConnectivity
        }
        guard let http = response as? HTTPURLResponse else { throw ApiError.noConnectivity }
        if http.statusCode == 401 { throw ApiError.unauthorized }
        if http.statusCode == 409, let conflict = try? JSONDecoder().decode(ConflictEnvelope.self, from: data) {
            throw ApiError.conflict(server: conflict.spot)
        }
        guard (200..<300).contains(http.statusCode) else {
            let message = (try? JSONDecoder().decode(ErrorEnvelope.self, from: data))?.error.message
            throw ApiError.http(statusCode: http.statusCode, message: message ?? HTTPURLResponse.localizedString(forStatusCode: http.statusCode))
        }
        return data
    }
}

private struct LoginBody: Encodable {
    let email: String
    let password: String
}

private struct ReviewBody: Encodable {
    let stars: Int
    let text: String
}

private struct ConflictEnvelope: Decodable {
    let spot: SpotDTO
}

private struct ErrorEnvelope: Decodable {
    struct Body: Decodable { let message: String }
    let error: Body
}
