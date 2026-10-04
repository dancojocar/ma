import SwiftUI

struct SpotListView: View {
    @State private var viewModel = SpotListViewModel()
    @State private var selectedSpot: Spot?

    var body: some View {
        NavigationStack {
            List {
                CategoryFilterView(
                    selectedCategory: viewModel.categoryFilter,
                    onCategoryChange: viewModel.onCategoryChange
                )
                .listRowInsets(EdgeInsets())
                .listRowSeparator(.hidden)

                ForEach(viewModel.filteredSpots) { spot in
                    Button {
                        selectedSpot = spot
                    } label: {
                        SpotRow(
                            spot: spot,
                            isFavourite: viewModel.favouriteIds.contains(spot.id),
                            onToggleFavourite: { viewModel.toggleFavourite(spot.id) }
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .listStyle(.plain)
            .navigationTitle("UniEats")
            .searchable(
                text: Binding(get: { viewModel.searchQuery }, set: viewModel.onQueryChange),
                prompt: "Search spots"
            )
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
