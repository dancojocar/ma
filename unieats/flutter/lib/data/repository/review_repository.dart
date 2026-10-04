import 'dart:convert';

import 'package:uuid/uuid.dart';

import '../../domain/models.dart';
import '../database/database.dart';
import '../network/api_client.dart';
import 'spot_repository.dart';

class ReviewRepository {
  ReviewRepository(this._db, this._api);

  final AppDatabase _db;
  final ApiClient _api;
  final _uuid = const Uuid();

  Stream<List<Review>> watchReviews(String spotId) => _db.watchReviews(spotId);

  Future<void> refreshReviews(String spotId) async =>
      _db.replaceReviews(spotId, await _api.fetchReviews(spotId));

  /// Shows the review at once under a `local:` id and queues it for the
  /// server, which assigns the real id and author when the outbox replays.
  Future<void> addReview(
    String spotId, {
    required int stars,
    required String text,
    required String author,
  }) => _db.transaction(() async {
    final localId = '$localIdPrefix${_uuid.v4()}';
    await _db.insertReview(
      Review(
        id: localId,
        spotId: spotId,
        author: author,
        stars: stars,
        text: text,
        createdAt: DateTime.now().millisecondsSinceEpoch,
      ),
    );
    await _db.enqueue(
      opId: _uuid.v4(),
      type: OutboxType.createReview,
      entityId: localId,
      payload: jsonEncode({'spotId': spotId, 'stars': stars, 'text': text}),
    );
  });
}
