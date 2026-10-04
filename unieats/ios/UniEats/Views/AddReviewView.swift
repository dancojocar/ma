import SwiftUI

struct AddReviewView: View {
    let onSubmit: (_ stars: Int, _ text: String) async -> String?
    @State private var stars = 5
    @State private var text = ""
    @State private var isSubmitting = false
    @State private var errorMessage: String?
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            Form {
                Section("Rating") {
                    Picker("Stars", selection: $stars) {
                        ForEach(1...5, id: \.self) { value in
                            Text(String(repeating: "★", count: value)).tag(value)
                        }
                    }
                    .pickerStyle(.segmented)
                }
                Section("Review") {
                    TextField("What did you think?", text: $text, axis: .vertical)
                        .lineLimit(3...6)
                }
                if let errorMessage {
                    Text(errorMessage).foregroundStyle(.red)
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
                            errorMessage = await onSubmit(stars, text)
                            isSubmitting = false
                            if errorMessage == nil { dismiss() }
                        }
                    }
                    .disabled(isSubmitting || text.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}
