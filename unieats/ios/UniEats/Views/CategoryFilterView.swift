import SwiftUI

struct CategoryFilterView: View {
    let selectedCategory: String?
    let onCategoryChange: (String?) -> Void

    private let categories: [(label: String, value: String?)] = [
        ("All", nil),
        ("Cafe", "cafe"),
        ("Canteen", "canteen"),
        ("Fast Food", "fastfood"),
        ("Bakery", "bakery"),
        ("Bar", "bar"),
    ]

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 10) {
                ForEach(categories, id: \.label) { item in
                    let isSelected = selectedCategory == item.value
                    Button {
                        onCategoryChange(item.value)
                    } label: {
                        Text(item.label)
                            .font(.subheadline)
                            .fontWeight(isSelected ? .semibold : .regular)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 8)
                            .background(isSelected ? Color.orange : Color(.systemGray5))
                            .foregroundStyle(isSelected ? .white : .primary)
                            .clipShape(Capsule())
                    }
                }
            }
            .padding(.horizontal)
        }
    }
}
