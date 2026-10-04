import SwiftUI

struct SpotDetailView: View {
    @State private var viewModel: SpotDetailViewModel

    init(spotId: String, api: ApiClient) {
        _viewModel = State(initialValue: SpotDetailViewModel(spotId: spotId, api: api))
    }

    var body: some View {
        Group {
            if let spot = viewModel.spot {
                SpotDetailContent(spot: spot, reviews: viewModel.reviews)
            } else if let message = viewModel.errorMessage {
                ContentUnavailableView {
                    Label("Couldn't load this spot", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(message)
                } actions: {
                    Button("Retry") { Task { await viewModel.load() } }
                        .buttonStyle(.borderedProminent)
                }
            } else {
                ProgressView()
            }
        }
        .navigationTitle(viewModel.spot?.name ?? "")
        .navigationBarTitleDisplayMode(.inline)
        .task { await viewModel.load() }
        .refreshable { await viewModel.load() }
    }
}

private struct SpotDetailContent: View {
    let spot: Spot
    let reviews: [Review]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                SpotPhoto(url: spot.photoUrl, iconSize: 60)
                    .frame(maxWidth: .infinity)
                    .frame(height: 260)
                    .clipped()

                VStack(alignment: .leading, spacing: 16) {
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(spot.name)
                                .font(.title.bold())
                            Spacer()
                            OpenBadge(openNow: spot.openNow)
                        }

                        HStack(spacing: 12) {
                            CategoryBadge(category: spot.category)
                            starRating
                            Text(String(repeating: "$", count: spot.priceLevel))
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                    }

                    Text(spot.spotDescription)
                        .font(.body)
                        .foregroundStyle(.secondary)

                    ReviewsSection(reviews: reviews)
                }
                .padding()
            }
        }
    }

    private var starRating: some View {
        HStack(spacing: 3) {
            ForEach(1...5, id: \.self) { star in
                Image(systemName: Double(star) <= spot.rating.rounded() ? "star.fill" : "star")
                    .font(.caption)
                    .foregroundStyle(.yellow)
            }
            Text(spot.rating, format: .number.precision(.fractionLength(1)))
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
    }
}
