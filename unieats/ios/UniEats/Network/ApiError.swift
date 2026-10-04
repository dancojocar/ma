import Foundation

enum ApiError: Error, LocalizedError {
    case http(statusCode: Int, message: String)
    case unauthorized
    case noConnectivity
    case timeout
    case decodingFailed(Error)
    case conflict(server: SpotDTO)

    var errorDescription: String? {
        switch self {
        case .http(let statusCode, let message):
            "Server error \(statusCode): \(message)"
        case .unauthorized:
            "Please log in again."
        case .noConnectivity:
            "Can't reach the server. Is it running (npm start in unieats/server)?"
        case .timeout:
            "The server took too long to answer."
        case .decodingFailed:
            "The server sent data the app could not read."
        case .conflict:
            "Someone else changed this spot first."
        }
    }
}
