import Foundation
import LocalAuthentication

protocol BiometricAuthenticating {
    /// True only when the device has biometrics enrolled (a passcode alone is not enough).
    var isAvailable: Bool { get }
    var kindName: String { get }
    /// Shows the system Face ID / Touch ID sheet and returns the evaluated context, which can then
    /// read a `.biometryCurrentSet` Keychain item without a second prompt.
    func authenticate(reason: String) async throws -> LAContext
    /// Changes whenever faces or fingers are enrolled or removed.
    func stateHash(of context: LAContext) -> Data?
}

struct BiometricAuth: BiometricAuthenticating {
    var isAvailable: Bool {
        LAContext().canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)
    }

    var kindName: String {
        let context = LAContext()
        _ = context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)
        return switch context.biometryType {
        case .faceID: "Face ID"
        case .touchID: "Touch ID"
        case .opticID: "Optic ID"
        default: "biometrics"
        }
    }

    func authenticate(reason: String) async throws -> LAContext {
        let context = LAContext()
        // Hides the system fallback button; the unlock screen offers the password route itself.
        context.localizedFallbackTitle = ""
        try await context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: reason)
        return context
    }

    func stateHash(of context: LAContext) -> Data? {
        context.domainState.biometry.stateHash
    }
}
