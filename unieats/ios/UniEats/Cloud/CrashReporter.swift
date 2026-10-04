import Foundation
import os

protocol CrashReporter: Sendable {
    func recordError(_ error: Error, context: String)
}

struct LoggingCrashReporter: CrashReporter {
    private let logger = Logger(subsystem: "com.unieats.app", category: "crash")

    func recordError(_ error: Error, context: String) {
        logger.error("\(context, privacy: .public): \(String(describing: error), privacy: .public)")
    }
}

@MainActor
@Observable
final class CrashReportingConsent {
    private static let key = "crash_reporting_consent"

    var isGranted: Bool {
        didSet { defaults.set(isGranted, forKey: Self.key) }
    }

    private let defaults: UserDefaults
    private let reporter: any CrashReporter

    init(reporter: any CrashReporter = LoggingCrashReporter(), defaults: UserDefaults = .standard) {
        self.reporter = reporter
        self.defaults = defaults
        isGranted = defaults.bool(forKey: Self.key)
    }

    @discardableResult
    func recordError(_ error: Error, context: String) -> Bool {
        guard isGranted else { return false }
        reporter.recordError(error, context: context)
        return true
    }
}

struct TestCrash: LocalizedError {
    var errorDescription: String? { "Test crash from the Settings screen" }
}
