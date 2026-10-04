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

  /// Optimistic edit, queued for the server as made by [userId].
  Future<void> editSpot(
    String id, {
    required String name,
    required String description,
    required bool openNow,
    required String userId,
  });

  Future<void> syncOutbox();

  /// Explicit sign-out: drops the outbox and every unsynced local change.
  Future<void> discardLocalChanges();

  /// Drops queued edits made by any user other than [userId].
  Future<void> discardChangesNotBy(String userId);
}

abstract interface class ReviewRepository {
  Stream<List<Review>> watchReviews(String spotId);

  Future<void> refreshReviews(String spotId);

  Future<void> addReview(
    String spotId, {
    required int stars,
    required String text,
    required User author,
  });
}

abstract interface class AuthRepository {
  Future<User> login(String email, String password);

  /// Explicit sign-out: the session and every unsynced local change go.
  Future<void> logout();

  /// The session ended without the user asking (401, biometrics changed):
  /// queued edits stay and replay once the same user signs in again.
  Future<void> endSession();

  /// The session stored on this device, restored without prompting.
  Future<Session> restoreSession();

  /// Prompts for biometrics. Throws [BiometricCancelled] (still locked), or
  /// [BiometricInvalidated] / [SessionExpired] after ending the session.
  Future<User> unlock();

  /// True when the device can do strong biometrics and has one enrolled.
  Future<bool> biometricsAvailable();

  Future<bool> biometricEnabled();

  Future<void> enableBiometric();

  Future<void> disableBiometric();
}
