import Foundation

/// The review being written. Resending the same content reuses the `Idempotency-Key`, so a retry
/// after a timeout or a 5xx cannot post it twice. Changed content gets a new key, because the
/// server answers a reused key with its cached response, even a cached 4xx.
struct ReviewDraft {
    var stars = 5 {
        didSet { if stars != oldValue { idempotencyKey = UUID().uuidString } }
    }
    var text = "" {
        didSet { if text != oldValue { idempotencyKey = UUID().uuidString } }
    }
    private(set) var idempotencyKey = UUID().uuidString

    var canSubmit: Bool { !text.trimmingCharacters(in: .whitespaces).isEmpty }
}
