import Foundation

@MainActor
@Observable
final class SessionStore {
    private(set) var token: String?
    private(set) var isLoggingIn = false
    private(set) var errorMessage: String?

    private let api: ApiClient
    private let keychain: KeychainHelper

    init(api: ApiClient, keychain: KeychainHelper = KeychainHelper()) {
        self.api = api
        self.keychain = keychain
        token = keychain.load()
    }

    var isLoggedIn: Bool { token != nil }

    func login(email: String, password: String) async {
        isLoggingIn = true
        defer { isLoggingIn = false }
        errorMessage = nil
        do {
            let response = try await api.login(email: email, password: password)
            try keychain.save(response.token)
            token = response.token
        } catch ApiError.unauthorized {
            errorMessage = "Wrong email or password."
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func logout() {
        keychain.delete()
        token = nil
    }

    func sessionExpired() {
        logout()
        errorMessage = "Your session expired. Please log in again."
    }
}
