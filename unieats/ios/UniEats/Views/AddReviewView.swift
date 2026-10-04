import SwiftUI

struct AddReviewView: View {
    let onSubmit: (_ draft: ReviewDraft) async -> String?
    @State private var draft = ReviewDraft()
    @State private var isSubmitting = false
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section("Rating") {
                    Picker("Stars", selection: $draft.stars) {
                        ForEach(1...5, id: \.self) { value in
                            Text(String(repeating: "★", count: value))
                                .accessibilityLabel("\(value) stars")
                                .tag(value)
                        }
                    }
                    .pickerStyle(.segmented)
                }
                Section("Review") {
                    TextField("What did you think?", text: $draft.text, axis: .vertical)
                        .lineLimit(3...6)
                }
                if let errorMessage {
                    Text(errorMessage).foregroundStyle(Color.closedText)
                }
            }
            .navigationTitle("Add review")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Post") {
                        isSubmitting = true
                        Task {
                            errorMessage = await onSubmit(draft)
                            isSubmitting = false
                            if errorMessage == nil { dismiss() }
                        }
                    }
                    .disabled(isSubmitting || !draft.canSubmit)
                }
            }
        }
    }
}
