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
    spots: (json['spots'] as List<dynamic>)
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

  @override
  bool operator ==(Object other) =>
      other is Review &&
      other.id == id &&
      other.spotId == spotId &&
      other.author == author &&
      other.stars == stars &&
      other.text == text &&
      other.createdAt == createdAt;

  @override
  int get hashCode => Object.hash(id, spotId, author, stars, text, createdAt);
}

class User {
  const User({
    required this.id,
    required this.email,
    required this.displayName,
  });

  factory User.fromJson(Map<String, dynamic> json) => User(
    id: json['id'] as String,
    email: json['email'] as String,
    displayName: json['displayName'] as String,
  );

  final String id;
  final String email;
  final String displayName;

  Map<String, dynamic> toJson() => {
    'id': id,
    'email': email,
    'displayName': displayName,
  };

  @override
  bool operator ==(Object other) =>
      other is User &&
      other.id == id &&
      other.email == email &&
      other.displayName == displayName;

  @override
  int get hashCode => Object.hash(id, email, displayName);
}

/// Where the app stands with the server: no session, a stored session that
/// needs biometrics before it can be used, or a usable session.
sealed class Session {
  const Session();
}

final class SignedOut extends Session {
  const SignedOut();

  @override
  bool operator ==(Object other) => other is SignedOut;

  @override
  int get hashCode => (SignedOut).hashCode;
}

final class Locked extends Session {
  const Locked(this.user);

  final User user;

  @override
  bool operator ==(Object other) => other is Locked && other.user == user;

  @override
  int get hashCode => Object.hash(Locked, user);
}

final class SignedIn extends Session {
  const SignedIn(this.user);

  final User user;

  @override
  bool operator ==(Object other) => other is SignedIn && other.user == user;

  @override
  int get hashCode => Object.hash(SignedIn, user);
}

class SessionExpired implements Exception {
  const SessionExpired();

  @override
  String toString() => 'Your session expired. Please sign in again.';
}

/// The biometric key is gone, e.g. a fingerprint or face was added.
class BiometricInvalidated implements Exception {
  const BiometricInvalidated();

  @override
  String toString() =>
      'Biometrics changed on this device. Please sign in again.';
}

class BiometricCancelled implements Exception {
  const BiometricCancelled();

  @override
  String toString() => 'Biometric prompt cancelled.';
}
