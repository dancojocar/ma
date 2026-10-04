import SwiftUI

struct SpotListView: View {
    @State private var selectedSpot: Spot?

    var body: some View {
        NavigationStack {
            List(sampleSpots) { spot in
                Button {
                    selectedSpot = spot
                } label: {
                    SpotRow(spot: spot)
                }
                .buttonStyle(.plain)
            }
            .listStyle(.plain)
            .navigationTitle("UniEats")
            .sheet(item: $selectedSpot) { spot in
                NavigationStack {
                    SpotDetailView(spot: spot)
                        .toolbar {
                            Button("Done") { selectedSpot = nil }
                        }
                }
            }
        }
    }
}

#Preview {
    SpotListView()
}
