import '../../domain/models.dart';
import '../database/database.dart';
import '../network/api_client.dart';

class ReviewRepository {
  ReviewRepository(this._db, this._api);

  final AppDatabase _db;
  final ApiClient _api;

  Stream<List<Review>> watchReviews(String spotId) => _db.watchReviews(spotId);

  Future<void> refreshReviews(String spotId) async =>
      _db.replaceReviews(spotId, await _api.fetchReviews(spotId));
}
