enum SpotCategory {
  cafe('Cafe'),
  canteen('Canteen'),
  fastfood('Fast food'),
  bakery('Bakery'),
  bar('Bar');

  const SpotCategory(this.label);

  final String label;
}

class Spot {
  const Spot({
    required this.id,
    required this.name,
    required this.category,
    required this.rating,
    required this.priceLevel,
    required this.lat,
    required this.lng,
    required this.openNow,
    required this.photoUrl,
    required this.description,
    required this.updatedAt,
  });

  final String id;
  final String name;
  final SpotCategory category;
  final double rating;
  final int priceLevel;
  final double lat;
  final double lng;
  final bool openNow;
  final String photoUrl;
  final String description;

  /// Epoch milliseconds, as in the shared contract.
  final int updatedAt;

  @override
  bool operator ==(Object other) =>
      other is Spot &&
      other.id == id &&
      other.name == name &&
      other.category == category &&
      other.rating == rating &&
      other.priceLevel == priceLevel &&
      other.lat == lat &&
      other.lng == lng &&
      other.openNow == openNow &&
      other.photoUrl == photoUrl &&
      other.description == description &&
      other.updatedAt == updatedAt;

  @override
  int get hashCode => Object.hash(
    id,
    name,
    category,
    rating,
    priceLevel,
    lat,
    lng,
    openNow,
    photoUrl,
    description,
    updatedAt,
  );
}
