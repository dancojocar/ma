import SwiftUI

struct SettingsView: View {
    let session: SessionStore
    let remoteConfig: RemoteConfig
    @Bindable var crashConsent: CrashReportingConsent
    @State private var testCrashResult: String?

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    LabeledContent("show_new_rating_ui", value: remoteConfig.showNewRatingUI ? "true" : "false")
                    Button {
                        Task { await remoteConfig.fetchAndActivate() }
                    } label: {
                        if remoteConfig.isFetching {
                            ProgressView()
                        } else {
                            Text("Fetch & activate")
                        }
                    }
                    .disabled(remoteConfig.isFetching)
                    if let lastFetched = remoteConfig.lastFetched {
                        LabeledContent("Last fetched", value: lastFetched.formatted(date: .omitted, time: .standard))
                    }
                    if let message = remoteConfig.errorMessage {
                        Text(message).foregroundStyle(.red)
                    }
                } header: {
                    Text("Remote config")
                } footer: {
                    Text("Flip the flag on the server with POST /api/config, then fetch & activate: the spot list shows the new rating badge.")
                }

                Section {
                    Toggle("Share crash reports", isOn: $crashConsent.isGranted)
                } header: {
                    Text("Privacy")
                } footer: {
                    Text("Nothing is reported until you opt in.")
                }

                #if DEBUG
                Section("Debug") {
                    Button("Test crash", role: .destructive) {
                        let sent = crashConsent.recordError(TestCrash(), context: "settings.testCrash")
                        testCrashResult = sent ? "Recorded (see the Xcode console)" : "Not sent: crash reporting is off"
                    }
                    if let testCrashResult {
                        Text(testCrashResult).font(.footnote).foregroundStyle(.secondary)
                    }
                }
                #endif

                Section("Account") {
                    Button("Log out", role: .destructive, action: session.logout)
                }
            }
            .navigationTitle("Settings")
        }
    }
}
