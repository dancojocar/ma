package com.unieats.app.data.repository

import com.unieats.shared.Category
import com.unieats.shared.Review
import com.unieats.shared.Spot
import kotlinx.coroutines.flow.Flow

/**
 * Everything the screens need from the data layer. ViewModels depend on this interface, so unit
 * tests can pass a fake instead of Room + Ktor (see FakeEatsRepository in app/src/test).
 */
interface EatsRepository {
    /** true while the live WebSocket is connected; collecting it keeps the socket open. */
    val liveConnection: Flow<Boolean>

    fun observeSpots(query: String, category: Category?): Flow<List<Spot>>
    fun observeSpot(id: String): Flow<Spot?>
    fun observeReviews(spotId: String): Flow<List<Review>>
    fun observePendingSpotIds(): Flow<Set<String>>
    fun observePendingChanges(): Flow<Int>

    /** Fetches one page into the local store and returns the server's hasNextPage. */
    suspend fun refreshSpots(page: Int, query: String, category: Category?): Result<Boolean>

    /** Every page, for screens that need the whole catalogue (Nearby). */
    suspend fun refreshAllSpots(): Result<Unit>
    suspend fun refreshSpot(id: String): Result<Unit>
    suspend fun refreshReviews(spotId: String): Result<Unit>
    suspend fun addReview(spotId: String, stars: Int, text: String): Result<Unit>

    /** Optimistic local edit + outbox entry; synced in the background. */
    suspend fun editSpot(id: String, name: String, description: String, openNow: Boolean)
}
