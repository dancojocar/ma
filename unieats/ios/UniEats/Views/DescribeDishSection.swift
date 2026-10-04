import SwiftUI

struct DescribeDishState {
    let text: String
    let isStreaming: Bool
    let error: String?
    let start: () -> Void
}

struct DescribeDishSection: View {
    let state: DescribeDishState

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Button(action: state.start) {
                Label(state.isStreaming ? "Describing…" : "Describe this dish", systemImage: "sparkles")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .tint(Color.aiText)
            .disabled(state.isStreaming)
            .accessibilityIdentifier("describe-dish-button")

            if !state.text.isEmpty || state.isStreaming {
                HStack(alignment: .top) {
                    Text(state.text.isEmpty ? " " : state.text)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .accessibilityIdentifier("describe-dish-text")
                    if state.isStreaming {
                        ProgressView()
                    }
                }
                .padding()
                .background(Color.aiText.opacity(0.06))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .animation(.easeOut(duration: 0.15), value: state.text)
                .accessibilityElement(children: .combine)
                .accessibilityAddTraits(state.isStreaming ? .updatesFrequently : [])
            }

            if let error = state.error {
                Label(error, systemImage: "exclamationmark.triangle")
                    .font(.footnote)
                    .foregroundStyle(Color.closedText)
            }
        }
        // VoiceOver would otherwise read nothing (or every chunk): announce the result once.
        .onChange(of: state.isStreaming) { _, isStreaming in
            guard !isStreaming, let result = state.error ?? (state.text.isEmpty ? nil : state.text) else { return }
            AccessibilityNotification.Announcement(result).post()
        }
    }
}
