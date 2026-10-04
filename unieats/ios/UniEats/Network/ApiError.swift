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
            String(localized: "Server error \(statusCode): \(message)")
        case .unauthorized:
            String(localized: "Please log in again.")
        case .noConnectivity:
            String(localized: "Can't reach the server. Is it running (npm start in unieats/server)?")
        case .timeout:
            String(localized: "The server took too long to answer.")
        case .decodingFailed:
            String(localized: "The server sent data the app could not read.")
        case .conflict:
            String(localized: "Someone else changed this spot first.")
        }
    }
}
