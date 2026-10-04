import SwiftUI

struct ReviewsSection: View {
    let reviews: [Review]
    let onAddReview: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Reviews (\(reviews.count))")
                    .font(.headline)
                    .accessibilityIdentifier("spot-detail-reviews")
                Spacer()
                Button("Add review", systemImage: "square.and.pencil", action: onAddReview)
                    .font(.subheadline)
            }

            if reviews.isEmpty {
                Text("No reviews yet.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
            }

            ForEach(reviews) { review in
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Text(review.author)
                            .font(.subheadline.weight(.semibold))
                        Spacer()
                        Text(String(repeating: "★", count: review.stars))
                            .foregroundStyle(.yellow)
                    }
                    Text(review.text)
                        .font(.subheadline)
                }
                .padding(.vertical, 4)
            }
        }
    }
}
