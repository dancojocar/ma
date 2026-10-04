import Foundation
import LocalAuthentication
import Security

enum KeychainError: Error {
    case unexpectedStatus(OSStatus)
}

/// Where `SessionStore` keeps the token; unit tests pass an in-memory one, because a simulator
/// build without code signing has no Keychain access.
protocol TokenStoring {
    func save(_ token: String, requireBiometry: Bool) throws(KeychainError)
    func load(context: LAContext?) throws(KeychainError) -> String?
    func hasItem() -> Bool
    func delete()
}

struct KeychainHelper: TokenStoring, Sendable {
    private let service = "com.unieats.app"
    private let account = "session.jwt"

    private var baseQuery: [String: Any] {
        [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: account,
        ]
    }

    /// Replaces the stored token. With `requireBiometry` the item can only be read after a
    /// biometric check, and enrolling a new face or finger invalidates it.
    func save(_ token: String, requireBiometry: Bool = false) throws(KeychainError) {
        // SecItemUpdate cannot change an item's access control, so always start from scratch.
        delete()
        var attributes = baseQuery
        attributes[kSecValueData as String] = Data(token.utf8)
        if requireBiometry {
            guard let access = SecAccessControlCreateWithFlags(
                nil, kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly, .biometryCurrentSet, nil
            ) else {
                throw .unexpectedStatus(errSecParam)
            }
            attributes[kSecAttrAccessControl as String] = access
        } else {
            attributes[kSecAttrAccessible as String] = kSecAttrAccessibleWhenUnlockedThisDeviceOnly
        }
        let status = SecItemAdd(attributes as CFDictionary, nil)
        guard status == errSecSuccess else { throw .unexpectedStatus(status) }
    }

    /// Never shows a biometric prompt: a protected item needs a context that has already been
    /// evaluated; without one, reading it fails with `errSecInteractionNotAllowed`.
    func load(context: LAContext? = nil) throws(KeychainError) -> String? {
        var query = baseQuery
        query[kSecReturnData as String] = true
        query[kSecMatchLimit as String] = kSecMatchLimitOne
        query[kSecUseAuthenticationContext as String] = context ?? Self.nonInteractiveContext()
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        if status == errSecItemNotFound { return nil }
        guard status == errSecSuccess, let data = result as? Data else { throw .unexpectedStatus(status) }
        return String(data: data, encoding: .utf8)
    }

    /// Whether a token is stored, without reading it and without prompting.
    func hasItem() -> Bool {
        var query = baseQuery
        query[kSecMatchLimit as String] = kSecMatchLimitOne
        query[kSecUseAuthenticationContext as String] = Self.nonInteractiveContext()
        let status = SecItemCopyMatching(query as CFDictionary, nil)
        return status == errSecSuccess || status == errSecInteractionNotAllowed
    }

    func delete() {
        SecItemDelete(baseQuery as CFDictionary)
    }

    private static func nonInteractiveContext() -> LAContext {
        let context = LAContext()
        context.interactionNotAllowed = true
        return context
    }
}
