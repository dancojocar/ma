import Foundation
import Network

@MainActor
@Observable
final class ConnectivityMonitor {
    private(set) var isOnline = true

    private let monitor = NWPathMonitor()
    private var isStarted = false

    func start(onReconnect: @escaping @MainActor () -> Void) {
        guard !isStarted else { return }
        isStarted = true
        monitor.pathUpdateHandler = { [weak self] path in
            let online = path.status == .satisfied
            Task { @MainActor in
                guard let self else { return }
                let reconnected = online && !self.isOnline
                self.isOnline = online
                if reconnected { onReconnect() }
            }
        }
        monitor.start(queue: DispatchQueue(label: "com.unieats.connectivity"))
    }
}
