import Foundation

struct User: Codable, Sendable {
    let id: String
    let email: String
    let displayName: String
}

struct LoginResponse: Decodable, Sendable {
    let token: String
    let user: User
}
