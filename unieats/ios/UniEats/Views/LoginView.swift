import SwiftUI

struct LoginView: View {
    let session: SessionStore
    @State private var email = "student@unieats.app"
    @State private var password = ""

    var body: some View {
        VStack(spacing: 24) {
            Spacer()

            Image(systemName: "fork.knife.circle.fill")
                .font(.system(size: 80))
                .foregroundStyle(.orange)

            Text("UniEats")
                .font(.largeTitle.bold())

            VStack(spacing: 16) {
                TextField("Email", text: $email)
                    .textContentType(.username)
                    .keyboardType(.emailAddress)
                    .autocorrectionDisabled()
                    .textInputAutocapitalization(.never)
                    .padding()
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .accessibilityIdentifier("login-email")

                SecureField("Password", text: $password)
                    .textContentType(.password)
                    .padding()
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .accessibilityIdentifier("login-password")
                    .onSubmit(logIn)
            }

            if let message = session.errorMessage {
                Text(message)
                    .foregroundStyle(.red)
                    .font(.callout)
                    .multilineTextAlignment(.center)
            }

            Button(action: logIn) {
                Group {
                    if session.isLoggingIn {
                        ProgressView().tint(.white)
                    } else {
                        Text("Log in").fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.orange)
                .foregroundStyle(.white)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .disabled(session.isLoggingIn || email.isEmpty || password.isEmpty)

            Text("Demo account: student@unieats.app / password")
                .font(.footnote)
                .foregroundStyle(.secondary)

            Spacer()
        }
        .padding(.horizontal, 32)
    }

    private func logIn() {
        Task { await session.login(email: email, password: password) }
    }
}
