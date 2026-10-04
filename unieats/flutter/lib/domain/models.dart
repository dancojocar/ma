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

  factory Spot.fromJson(Map<String, dynamic> json) => Spot(
    id: json['id'] as String,
    name: json['name'] as String,
    category: SpotCategory.values.byName(json['category'] as String),
    rating: (json['rating'] as num).toDouble(),
    priceLevel: (json['priceLevel'] as num).toInt(),
    lat: (json['lat'] as num).toDouble(),
    lng: (json['lng'] as num).toDouble(),
    openNow: json['openNow'] as bool,
    photoUrl: json['photoUrl'] as String? ?? '',
    description: json['description'] as String? ?? '',
    updatedAt: (json['updatedAt'] as num).toInt(),
  );

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

  Map<String, dynamic> toJson() => {
    'id': id,
    'name': name,
    'category': category.name,
    'rating': rating,
    'priceLevel': priceLevel,
    'lat': lat,
    'lng': lng,
    'openNow': openNow,
    'photoUrl': photoUrl,
    'description': description,
    'updatedAt': updatedAt,
  };
}

class SpotsPage {
  const SpotsPage({
    required this.spots,
    required this.page,
    required this.hasNextPage,
  });

  factory SpotsPage.fromJson(Map<String, dynamic> json) => SpotsPage(
    spots:
        (json['spots'] as List<dynamic>)
            .map((e) => Spot.fromJson(e as Map<String, dynamic>))
            .toList(),
    page: (json['page'] as num).toInt(),
    hasNextPage: json['hasNextPage'] as bool,
  );

  final List<Spot> spots;
  final int page;
  final bool hasNextPage;
}

class Review {
  const Review({
    required this.id,
    required this.spotId,
    required this.author,
    required this.stars,
    required this.text,
    required this.createdAt,
  });

  factory Review.fromJson(Map<String, dynamic> json) => Review(
    id: json['id'] as String,
    spotId: json['spotId'] as String,
    author: json['author'] as String,
    stars: (json['stars'] as num).toInt(),
    text: json['text'] as String? ?? '',
    createdAt: (json['createdAt'] as num).toInt(),
  );

  final String id;
  final String spotId;
  final String author;
  final int stars;
  final String text;
  final int createdAt;
}
