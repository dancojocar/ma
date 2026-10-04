import SwiftUI

struct ReviewsSection: View {
    let reviews: [Review]

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Reviews (\(reviews.count))")
                .font(.headline)

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
