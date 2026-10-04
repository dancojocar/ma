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
}
