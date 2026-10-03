import SwiftUI

struct SpotPhoto: View {
    let url: String
    var iconSize: CGFloat = 24

    var body: some View {
        AsyncImage(url: URL(string: url)) { phase in
            if let image = phase.image {
                image.resizable().scaledToFill()
            } else {
                Color(.systemGray5)
                    .overlay {
                        Image(systemName: "fork.knife")
                            .font(.system(size: iconSize))
                            .foregroundStyle(.secondary)
                    }
            }
        }
    }
}
