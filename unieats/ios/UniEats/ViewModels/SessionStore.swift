import Foundation
import LocalAuthentication

@Observable
final class SessionStore {
    enum Phase {
        case signedOut
        /// A session is stored behind biometrics and has not been unlocked since launch.
        case locked
        case signedIn
    }

    private(set) var phase: Phase = .signedOut
    private(set) var token: String?
    /// `user.id` from the login response; every queued edit records it.
    private(set) var userId: String?
    private(set) var biometricUnlockEnabled: Bool
    private(set) var isLoggingIn = false
    private(set) var isUnlocking = false
    private(set) var errorMessage: String?

    let biometrics: any BiometricAuthenticating
    private let api: ApiClient
    private let keychain: KeychainHelper
    private let defaults: UserDefaults

    init(
        api: ApiClient,
        keychain: KeychainHelper = KeychainHelper(),
        defaults: UserDefaults = .standard,
        biometrics: any BiometricAuthenticating = BiometricAuth()
    ) {
        self.api = api
        self.keychain = keychain
        self.defaults = defaults
        self.biometrics = biometrics
        userId = defaults.string(forKey: Key.userId)
        biometricUnlockEnabled = defaults.bool(forKey: Key.biometricUnlock)
        restore()
    }

    var isLoggedIn: Bool { phase == .signedIn }

    func login(email: String, password: String) async {
        isLoggingIn = true
        defer { isLoggingIn = false }
        errorMessage = nil
        do {
            let response = try await api.login(email: email, password: password)
            try keychain.save(response.token)
            token = response.token
            userId = response.user.id
            defaults.set(response.user.id, forKey: Key.userId)
            phase = .signedIn
        } catch ApiError.unauthorized {
            errorMessage = "Wrong email or password."
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func unlock() async {
        guard phase == .locked, !isUnlocking else { return }
        isUnlocking = true
        defer { isUnlocking = false }
        errorMessage = nil

        let context: LAContext
        do {
            context = try await biometrics.authenticate(reason: "Unlock UniEats")
        } catch let error as LAError where Self.staysLocked.contains(error.code) {
            return
        } catch {
            return end(notice: "\(biometrics.kindName) is not available. Please log in with your password.")
        }

        // A new face or finger changes the state hash and invalidates the .biometryCurrentSet item.
        guard biometrics.stateHash(of: context) == storedStateHash,
              let stored = try? keychain.load(context: context) else {
            return end(notice: "Biometrics changed on this device. Please log in with your password.")
        }
        token = stored
        phase = .signedIn
    }

    func setBiometricUnlock(_ on: Bool) async {
        guard phase == .signedIn, let token, on != biometricUnlockEnabled else { return }
        if on {
            let reason = "Use \(biometrics.kindName) to unlock UniEats"
            guard let context = try? await biometrics.authenticate(reason: reason),
                  phase == .signedIn, self.token == token else { return }
            do {
                try keychain.save(token, requireBiometry: true)
            } catch {
                try? keychain.save(token)
                return
            }
            storedStateHash = biometrics.stateHash(of: context)
        } else {
            try? keychain.save(token)
            storedStateHash = nil
        }
        biometricUnlockEnabled = on
        defaults.set(on, forKey: Key.biometricUnlock)
    }

    /// Ends the session only. Use `SpotRepository.logOut()` for an explicit log out, which also
    /// discards edits that have not synced.
    func logout() {
        keychain.delete()
        token = nil
        userId = nil
        biometricUnlockEnabled = false
        storedStateHash = nil
        defaults.removeObject(forKey: Key.userId)
        defaults.removeObject(forKey: Key.biometricUnlock)
        phase = .signedOut
    }

    func sessionExpired() {
        end(notice: "Your session expired. Please log in again.")
    }

    private func end(notice: String) {
        logout()
        errorMessage = notice
    }

    /// Keychain items survive deleting the app but UserDefaults do not, so a stored session is
    /// only used together with its user id and biometric flag.
    private func restore() {
        guard userId != nil else { return logout() }
        if biometricUnlockEnabled {
            if keychain.hasItem() { phase = .locked } else { logout() }
        } else if let stored = try? keychain.load() {
            token = stored
            phase = .signedIn
        } else {
            logout()
        }
    }

    private var storedStateHash: Data? {
        get { defaults.data(forKey: Key.biometryStateHash) }
        set { defaults.set(newValue, forKey: Key.biometryStateHash) }
    }

    private static let staysLocked: Set<LAError.Code> = [
        .userCancel, .userFallback, .systemCancel, .appCancel, .authenticationFailed, .notInteractive,
    ]

    private enum Key {
        static let userId = "session.userId"
        static let biometricUnlock = "session.biometricUnlock"
        static let biometryStateHash = "session.biometryStateHash"
    }
}
