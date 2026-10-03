import SwiftUI

struct EditSpotView: View {
    let onSave: (_ name: String, _ description: String, _ openNow: Bool) -> Void
    @State private var name: String
    @State private var description: String
    @State private var openNow: Bool
    @Environment(\.dismiss) private var dismiss

    init(spot: Spot, onSave: @escaping (_ name: String, _ description: String, _ openNow: Bool) -> Void) {
        self.onSave = onSave
        _name = State(initialValue: spot.name)
        _description = State(initialValue: spot.spotDescription)
        _openNow = State(initialValue: spot.openNow)
    }

    var body: some View {
        NavigationStack {
            Form {
                TextField("Name", text: $name)
                    .accessibilityIdentifier("edit-spot-name")
                TextField("Description", text: $description, axis: .vertical)
                    .lineLimit(3...6)
                    .accessibilityIdentifier("edit-spot-description")
                Toggle("Open now", isOn: $openNow)
            }
            .navigationTitle("Edit spot")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        onSave(name.trimmingCharacters(in: .whitespaces), description, openNow)
                        dismiss()
                    }
                    .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
        }
    }
}
