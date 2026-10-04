import SwiftUI

struct SpotListView: View {
    @State private var viewModel = SpotListViewModel()
    @State private var path: [String] = []

    var body: some View {
        NavigationStack(path: $path) {
            List {
                CategoryFilterView(
                    selectedCategory: viewModel.categoryFilter,
                    onCategoryChange: viewModel.onCategoryChange
                )
                .listRowInsets(EdgeInsets())
                .listRowSeparator(.hidden)

                ForEach(viewModel.filteredSpots) { spot in
                    NavigationLink(value: spot.id) {
                        SpotRow(
                            spot: spot,
                            isFavourite: viewModel.favouriteIds.contains(spot.id),
                            onToggleFavourite: { viewModel.toggleFavourite(spot.id) }
                        )
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("UniEats")
            .searchable(
                text: Binding(get: { viewModel.searchQuery }, set: viewModel.onQueryChange),
                prompt: "Search spots"
            )
            .navigationDestination(for: String.self) { spotId in
                SpotDetailView(spotId: spotId)
            }
        }
        .onOpenURL { url in
            if let spotId = DeepLink.spotId(from: url) {
                path = [spotId]
            }
        }
    }
}

#Preview {
    SpotListView()
}
