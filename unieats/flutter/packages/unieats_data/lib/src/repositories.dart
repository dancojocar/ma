import 'live_event.dart';
import 'models.dart';

/// What the UI needs from spot data. The app implements it on top of Drift
/// and dio (`DriftSpotRepository`); tests can implement it in memory.
abstract interface class SpotRepository {
  Stream<List<Spot>> watchSpots({String query = '', SpotCategory? category});

  Stream<Spot?> watchSpot(String id);

  /// Ids of spots (and local reviews) with changes waiting in the outbox.
  Stream<Set<String>> watchPendingIds();

  Stream<int> watchPendingCount();

  /// Fetches one page from the server into the local store.
  Future<SpotsPage> refreshPage({
    required int page,
    required int limit,
    String query = '',
    SpotCategory? category,
  });

  Future<void> refreshSpot(String id);

  Future<void> applyLiveEvent(LiveEvent event);

  Future<void> editSpot(
    String id, {
    required String name,
    required String description,
    required bool openNow,
  });

  Future<void> syncOutbox();
}

abstract interface class ReviewRepository {
  Stream<List<Review>> watchReviews(String spotId);

  Future<void> refreshReviews(String spotId);

  Future<void> addReview(
    String spotId, {
    required int stars,
    required String text,
    required String author,
  });
}

abstract interface class AuthRepository {
  Future<User> login(String email, String password);

  Future<void> logout();

  /// The signed-in user from a stored, unexpired session, or null.
  Future<User?> restoreSession();
}
