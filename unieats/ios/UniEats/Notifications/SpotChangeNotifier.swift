import Foundation
import UserNotifications

final class SpotChangeNotifier: NSObject, UNUserNotificationCenterDelegate, Sendable {
    private var center: UNUserNotificationCenter { .current() }

    func requestPermission() async {
        center.delegate = self
        _ = try? await center.requestAuthorization(options: [.alert, .sound])
    }

    func notifySpotUpdated(_ spot: Spot) {
        let content = UNMutableNotificationContent()
        content.title = "UniEats"
        content.body = "\(spot.name) was updated"
        content.sound = .default
        let request = UNNotificationRequest(identifier: "spot-updated-\(spot.id)", content: content, trigger: nil)
        center.add(request)
    }

    // Live updates only arrive while the app is in the foreground, where iOS hides
    // notifications unless the delegate asks for a banner.
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        [.banner, .sound]
    }
}
