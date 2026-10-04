import SwiftUI

struct UnlockView: View {
    let session: SessionStore
    let onUsePassword: () -> Void

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "lock.fill")
                .font(.system(size: 64))
                .foregroundStyle(.orange)
                .accessibilityHidden(true)

            Text("Unlock UniEats")
                .font(.largeTitle.bold())

            Button {
                Task { await session.unlock() }
            } label: {
                Label("Unlock with \(session.biometrics.kindName)", systemImage: "lock.open.fill")
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.brandFill)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .disabled(session.isUnlocking)
            .accessibilityIdentifier("unlock-button")

            Button("Use password instead", action: onUsePassword)
                .accessibilityIdentifier("unlock-use-password")

            Spacer()
        }
        .padding(.horizontal, 32)
        .task { await session.unlock() }
    }
}
