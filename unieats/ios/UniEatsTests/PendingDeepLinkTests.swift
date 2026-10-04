import Foundation
import Testing
@testable import UniEats

@Suite("A deep link opened while logged out or locked")
struct PendingDeepLinkTests {
    private let backend = SpotBackend(spots: [])
    private let defaults = isolatedDefaults()
    private let tokens = InMemoryTokenStore()
    private let link = URL(string: "unieats://spots/spot-3")!

    private func launch() -> SessionStore {
        SessionStore(
            api: ApiClient(baseURL: backend.server.baseURL, configuration: backend.server.configuration),
            tokens: tokens, defaults: defaults, biometrics: FakeBiometrics()
        )
    }

    @Test("is kept on the login screen and shown once after login")
    func appliedAfterLogin() async {
        let session = launch()
        let router = AppRouter(session: session)

        router.open(link)
        #expect(router.takePendingSpotId() == nil)
        #expect(router.pendingSpotId == "spot-3")

        await session.login(email: "student@unieats.app", password: "password")
        #expect(router.takePendingSpotId() == "spot-3")
        #expect(router.takePendingSpotId() == nil)
    }

    @Test("is kept on the unlock screen and shown after unlock")
    func appliedAfterUnlock() async {
        let first = launch()
        await first.login(email: "student@unieats.app", password: "password")
        await first.setBiometricUnlock(true)
        let session = launch()
        #expect(session.phase == .locked)
        let router = AppRouter(session: session)

        router.open(URL(string: "https://unieats.app/spots/spot-3")!)
        #expect(router.takePendingSpotId() == nil)

        await session.unlock()
        #expect(router.takePendingSpotId() == "spot-3")
    }

    @Test("opened after a log out, waits for the next login; an invalid link is ignored")
    func waitsAfterLogOut() async {
        let session = launch()
        await session.login(email: "student@unieats.app", password: "password")
        session.logout()
        let router = AppRouter(session: session)

        router.open(URL(string: "unieats://reviews/spot-3")!)
        #expect(router.pendingSpotId == nil)

        router.open(link)
        await session.login(email: "student@unieats.app", password: "password")
        #expect(router.takePendingSpotId() == "spot-3")
    }

    @Test("selects the Spots tab; other links leave the tab alone")
    func selectsSpotsTab() {
        let router = AppRouter(session: launch())
        router.tab = .settings

        router.open(URL(string: "https://example.com/spots/spot-3")!)
        #expect(router.tab == .settings)

        router.open(link)
        #expect(router.tab == .spots)
    }

    @Test("the tab goes back to Spots when the session ends")
    func sessionEndResetsTab() {
        let router = AppRouter(session: launch())
        router.tab = .settings
        router.sessionEnded()
        #expect(router.tab == .spots)
    }
}
