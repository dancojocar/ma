import SwiftData
import SwiftUI

struct SpotDetailView: View {
    @State private var viewModel: SpotDetailViewModel
    @State private var isEditing = false
    @State private var isAddingReview = false
    @Query private var spots: [SpotEntity]
    @Query private var reviewEntities: [ReviewEntity]

    init(spotId: String, repository: SpotRepository) {
        _viewModel = State(initialValue: SpotDetailViewModel(spotId: spotId, repository: repository))
        _spots = Query(filter: #Predicate<SpotEntity> { $0.id == spotId })
        _reviewEntities = Query(
            filter: #Predicate<ReviewEntity> { $0.spotId == spotId },
            sort: \.createdAt, order: .reverse
        )
    }

    var body: some View {
        Group {
            if let entity = spots.first {
                SpotDetailContent(
                    spot: entity.spot,
                    isPending: entity.pendingSync,
                    reviews: reviewEntities.map(Review.init(entity:)),
                    onAddReview: { isAddingReview = true }
                )
                .sheet(isPresented: $isAddingReview) {
                    AddReviewView(onSubmit: viewModel.addReview)
                }
            } else if let message = viewModel.errorMessage {
                ContentUnavailableView {
                    Label("Couldn't load this spot", systemImage: "wifi.exclamationmark")
                } description: {
                    Text(message)
                } actions: {
                    Button("Retry") { Task { await viewModel.refresh() } }
                        .buttonStyle(.borderedProminent)
                }
            } else {
                ProgressView()
            }
        }
        .navigationTitle(spots.first?.name ?? "")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if let entity = spots.first {
                Button("Edit spot") { isEditing = true }
                    .sheet(isPresented: $isEditing) {
                        EditSpotView(spot: entity.spot, onSave: viewModel.save)
                    }
            }
        }
        .task { await viewModel.refresh() }
        .refreshable { await viewModel.refresh() }
    }
}

private struct SpotDetailContent: View {
    let spot: Spot
    let isPending: Bool
    let reviews: [Review]
    let onAddReview: () -> Void

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

                    if isPending {
                        Label("Saved on this device, waiting to sync", systemImage: "clock.arrow.circlepath")
                            .font(.footnote)
                            .foregroundStyle(.orange)
                    }

                    Text(spot.spotDescription)
                        .font(.body)
                        .foregroundStyle(.secondary)

                    ReviewsSection(reviews: reviews, onAddReview: onAddReview)
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
