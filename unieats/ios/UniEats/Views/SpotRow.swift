import SwiftUI

struct SpotRow: View {
    @Environment(RemoteConfig.self) private var remoteConfig
    let spot: Spot
    let isFavourite: Bool
    var isPending = false
    let photoTransition: Namespace.ID
    let onToggleFavourite: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            SpotPhoto(url: spot.photoUrl)
                .frame(width: 64, height: 64)
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .matchedTransitionSource(id: spot.id, in: photoTransition)

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(spot.name)
                        .font(.headline)
                        .lineLimit(1)
                    if isPending {
                        Image(systemName: "clock.arrow.circlepath")
                            .font(.caption)
                            .foregroundStyle(.orange)
                            .accessibilityLabel("Pending sync")
                    }
                    Spacer()
                    Button(action: onToggleFavourite) {
                        Image(systemName: isFavourite ? "heart.fill" : "heart")
                            .foregroundStyle(isFavourite ? .red : .secondary)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(isFavourite ? "Remove from favourites" : "Add to favourites")
                    OpenBadge(openNow: spot.openNow)
                }

                HStack(spacing: 6) {
                    CategoryBadge(category: spot.category)
                    if remoteConfig.showNewRatingUI {
                        NewRatingBadge(rating: spot.rating)
                    } else {
                        starRating
                    }
                    priceLevel
                }
            }
        }
        .padding(.vertical, 4)
    }

    private var starRating: some View {
        HStack(spacing: 2) {
            Image(systemName: "star.fill")
                .font(.caption2)
                .foregroundStyle(.yellow)
            Text(spot.rating, format: .number.precision(.fractionLength(1)))
                .font(.caption)
        }
    }

    private var priceLevel: some View {
        Text(String(repeating: "$", count: spot.priceLevel))
            .font(.caption)
            .foregroundStyle(.secondary)
    }
}

struct OpenBadge: View {
    let openNow: Bool

    var body: some View {
        Text(openNow ? "Open" : "Closed")
            .font(.caption2.weight(.medium))
            .padding(.horizontal, 7)
            .padding(.vertical, 3)
            .background(openNow ? Color.green.opacity(0.15) : Color.red.opacity(0.1))
            .foregroundStyle(openNow ? .green : .red)
            .clipShape(Capsule())
    }
}

struct CategoryBadge: View {
    let category: String

    var body: some View {
        Text(category.capitalized)
            .font(.caption)
            .padding(.horizontal, 8)
            .padding(.vertical, 3)
            .background(Color.orange.opacity(0.15))
            .foregroundStyle(.orange)
            .clipShape(Capsule())
    }
}


struct NewRatingBadge: View {
    let rating: Double

    var body: some View {
        HStack(spacing: 3) {
            Image(systemName: "star.circle.fill")
            Text(rating, format: .number.precision(.fractionLength(1)))
            Text("NEW").font(.caption2.weight(.heavy))
        }
        .font(.caption.weight(.semibold))
        .padding(.horizontal, 8)
        .padding(.vertical, 3)
        .background(LinearGradient(colors: [.orange, .pink], startPoint: .leading, endPoint: .trailing))
        .foregroundStyle(.white)
        .clipShape(Capsule())
        .accessibilityLabel("Rating \(rating.formatted(.number.precision(.fractionLength(1)))), new rating badge")
    }
}
