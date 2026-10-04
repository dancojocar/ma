import SwiftUI

struct SpotDetailView: View {
    let spotId: String

    var body: some View {
        if let spot = sampleSpots.first(where: { $0.id == spotId }) {
            SpotDetailContent(spot: spot)
        } else {
            ContentUnavailableView("Spot not found", systemImage: "fork.knife", description: Text(spotId))
        }
    }
}

private struct SpotDetailContent: View {
    let spot: Spot

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
                }
                .padding()
            }
        }
        .navigationTitle(spot.name)
        .navigationBarTitleDisplayMode(.inline)
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
