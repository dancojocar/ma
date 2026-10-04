import Foundation

enum DeepLink {
    static let webHost = "unieats.app"

    static func spotId(from url: URL) -> String? {
        let segments: [String]
        switch url.scheme {
        case "unieats":
            segments = [url.host() ?? ""] + url.pathComponents.filter { $0 != "/" }
        case "https" where url.host() == webHost:
            segments = url.pathComponents.filter { $0 != "/" }
        default:
            return nil
        }
        guard segments.count == 2, segments[0] == "spots", !segments[1].isEmpty else { return nil }
        return segments[1]
    }
}
