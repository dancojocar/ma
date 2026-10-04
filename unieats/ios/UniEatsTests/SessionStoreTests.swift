import Foundation
import LocalAuthentication
import Testing
@testable import UniEats

/// The Keychain access control itself (`.biometryCurrentSet`) needs a signed app and a device or an
/// enrolled simulator; README › l08 has the manual check. These tests cover the session logic.
@Suite("SessionStore phases and biometric unlock")
struct SessionStoreTests {
    private let backend = SpotBackend(spots: [])
    private let defaults = isolatedDefaults()
    private let tokens = InMemoryTokenStore()
    private let biometrics = FakeBiometrics()

    /// A new store over the same storage is what the next launch of the app sees.
    private func launch() -> SessionStore {
        SessionStore(
            api: ApiClient(baseURL: backend.server.baseURL, configuration: backend.server.configuration),
            tokens: tokens, defaults: defaults, biometrics: biometrics
        )
    }

    private func signedInWithBiometrics() async -> SessionStore {
        let session = launch()
        await session.login(email: "student@unieats.app", password: "password")
        await session.setBiometricUnlock(true)
        return launch()
    }

    @Test("starts signed out when nothing is stored")
    func startsSignedOut() {
        let session = launch()
        #expect(session.phase == .signedOut)
        #expect(session.token == nil)
    }

    @Test("login stores the token and the user id; the next launch is signed in without a prompt")
    func loginAndRestore() async {
        let session = launch()
        await session.login(email: "student@unieats.app", password: "password")

        #expect(session.phase == .signedIn)
        #expect(session.userId == "user-student")
        #expect(tokens.token == "token-user-student")
        #expect(!tokens.requiresBiometry)

        let relaunched = launch()
        #expect(relaunched.phase == .signedIn)
        #expect(relaunched.token == "token-user-student")
        #expect(biometrics.prompts == 0)
    }

    @Test("wrong credentials stay signed out with a message")
    func wrongPassword() async {
        let session = launch()
        await session.login(email: "student@unieats.app", password: "nope")

        #expect(session.phase == .signedOut)
        #expect(session.errorMessage == String(localized: "Wrong email or password."))
        #expect(tokens.token == nil)
    }

    @Test("a token left after reinstalling the app (no user id in UserDefaults) is not used")
    func tokenWithoutUserIsDiscarded() throws {
        try tokens.save("token-from-before", requireBiometry: false)

        #expect(launch().phase == .signedOut)
        #expect(tokens.token == nil)
    }

    @Test("turning biometric unlock on prompts once and stores the token behind biometry")
    func enableBiometricUnlock() async {
        let session = launch()
        await session.login(email: "student@unieats.app", password: "password")
        await session.setBiometricUnlock(true)

        #expect(session.biometricUnlockEnabled)
        #expect(biometrics.prompts == 1)
        #expect(tokens.requiresBiometry)
        #expect(session.phase == .signedIn)
    }

    @Test("a cancelled prompt leaves biometric unlock off")
    func cancelledEnable() async {
        let session = launch()
        await session.login(email: "student@unieats.app", password: "password")
        biometrics.failure = LAError(.userCancel)
        await session.setBiometricUnlock(true)

        #expect(!session.biometricUnlockEnabled)
        #expect(!tokens.requiresBiometry)
    }

    @Test("with biometric unlock on, a launch starts locked without reading the token or prompting")
    func launchIsLocked() async {
        let session = await signedInWithBiometrics()

        #expect(session.phase == .locked)
        #expect(session.token == nil)
        #expect(biometrics.prompts == 1)
    }

    @Test("unlocking with the same enrolment signs in with the stored token")
    func unlock() async {
        let session = await signedInWithBiometrics()
        await session.unlock()

        #expect(session.phase == .signedIn)
        #expect(session.token == "token-user-student")
        #expect(session.userId == "user-student")
    }

    @Test("a cancelled or failed prompt stays on the unlock screen", arguments: [
        LAError.Code.userCancel, .authenticationFailed, .systemCancel,
    ])
    func unlockCancelled(_ code: LAError.Code) async {
        let session = await signedInWithBiometrics()
        biometrics.failure = LAError(code)
        await session.unlock()

        #expect(session.phase == .locked)
        #expect(tokens.token != nil)
    }

    @Test("a new face or finger invalidates the session: signed out with a notice")
    func enrolmentChanged() async {
        let session = await signedInWithBiometrics()
        biometrics.enrolment = Data("face-2".utf8)
        await session.unlock()

        #expect(session.phase == .signedOut)
        #expect(session.errorMessage != nil)
        #expect(!session.biometricUnlockEnabled)
        #expect(tokens.token == nil)
        #expect(launch().phase == .signedOut)
    }

    @Test("biometrics no longer available: signed out with a notice")
    func biometricsUnavailable() async {
        let session = await signedInWithBiometrics()
        biometrics.failure = LAError(.biometryNotEnrolled)
        await session.unlock()

        #expect(session.phase == .signedOut)
        #expect(session.errorMessage != nil)
        #expect(tokens.token == nil)
    }

    @Test("turning biometric unlock off stores the token normally again")
    func disableBiometricUnlock() async {
        let session = await signedInWithBiometrics()
        await session.unlock()
        await session.setBiometricUnlock(false)

        #expect(!session.biometricUnlockEnabled)
        #expect(!tokens.requiresBiometry)
        #expect(launch().phase == .signedIn)
    }

    @Test("logout deletes the token and the biometric setting")
    func logout() async {
        let session = await signedInWithBiometrics()
        await session.unlock()
        session.logout()

        #expect(session.phase == .signedOut)
        #expect(session.token == nil)
        #expect(session.userId == nil)
        #expect(!session.biometricUnlockEnabled)
        #expect(tokens.token == nil)
        #expect(launch().phase == .signedOut)
    }

    @Test("a 401 (session expired) signs out with a notice and deletes the biometric item")
    func sessionExpired() async {
        let session = await signedInWithBiometrics()
        await session.unlock()
        session.sessionExpired()

        #expect(session.phase == .signedOut)
        #expect(session.errorMessage != nil)
        #expect(tokens.token == nil)
        #expect(!session.biometricUnlockEnabled)
    }
}
