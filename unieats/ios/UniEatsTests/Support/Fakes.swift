import Foundation
import LocalAuthentication
@testable import UniEats

/// Behaves like the Keychain item: a biometry-protected token is only readable with an evaluated
/// context.
final class InMemoryTokenStore: TokenStoring {
    private(set) var token: String?
    private(set) var requiresBiometry = false

    func save(_ token: String, requireBiometry: Bool) throws(KeychainError) {
        self.token = token
        requiresBiometry = requireBiometry
    }

    func load(context: LAContext?) throws(KeychainError) -> String? {
        guard let token else { return nil }
        if requiresBiometry, context == nil { throw .unexpectedStatus(errSecInteractionNotAllowed) }
        return token
    }

    func hasItem() -> Bool { token != nil }

    func delete() {
        token = nil
        requiresBiometry = false
    }
}

final class FakeBiometrics: BiometricAuthenticating {
    var isAvailable = true
    var kindName = "Face ID"
    /// What the next prompt does: nil succeeds, an error fails it.
    var failure: (any Error)?
    /// Changing it simulates enrolling a new face or finger.
    var enrolment = Data("face-1".utf8)
    private(set) var prompts = 0

    func authenticate(reason: String) async throws -> LAContext {
        prompts += 1
        if let failure { throw failure }
        return LAContext()
    }

    func stateHash(of context: LAContext) -> Data? { enrolment }
}

/// Holds a stubbed response until the test releases it.
final class Gate {
    private var arrival: CheckedContinuation<Void, Never>?
    private var release: CheckedContinuation<Void, Never>?
    private var hasArrived = false

    /// Called by the stub handler.
    func pass() async {
        hasArrived = true
        arrival?.resume()
        arrival = nil
        await withCheckedContinuation { release = $0 }
    }

    func waitForArrival() async {
        guard !hasArrived else { return }
        await withCheckedContinuation { arrival = $0 }
    }

    func open() {
        release?.resume()
        release = nil
    }
}

func isolatedDefaults() -> UserDefaults {
    let name = "UniEatsTests.\(UUID().uuidString)"
    let defaults = UserDefaults(suiteName: name)!
    defaults.removePersistentDomain(forName: name)
    return defaults
}
