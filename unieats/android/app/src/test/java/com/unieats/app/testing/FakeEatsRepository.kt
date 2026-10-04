package com.unieats.app.testing

import com.unieats.app.data.repository.EatsRepository
import com.unieats.shared.Category
import com.unieats.shared.Review
import com.unieats.shared.Spot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.io.IOException

/** In-memory EatsRepository: a list of spots, a call log and a switch to make refreshes fail. */
class FakeEatsRepository(spots: List<Spot> = sampleSpots) : EatsRepository {

    val spots = MutableStateFlow(spots)
    val refreshCalls = mutableListOf<RefreshCall>()
    var failRefresh = false
    var hasNextPage = false

    data class RefreshCall(val page: Int, val query: String, val category: Category?)

    override val liveConnection: Flow<Boolean> = flowOf(true)

    override fun observeSpots(query: String, category: Category?): Flow<List<Spot>> = spots.map { all ->
        all.filter {
            it.name.contains(query, ignoreCase = true) && (category == null || it.category == category)
        }
    }

    override fun observeSpot(id: String): Flow<Spot?> = spots.map { all -> all.firstOrNull { it.id == id } }

    override fun observeReviews(spotId: String): Flow<List<Review>> = flowOf(emptyList())

    override fun observePendingSpotIds(): Flow<Set<String>> = flowOf(emptySet())

    override fun observePendingChanges(): Flow<Int> = flowOf(0)

    override suspend fun refreshSpots(page: Int, query: String, category: Category?): Result<Boolean> {
        refreshCalls += RefreshCall(page, query, category)
        return if (failRefresh) Result.failure(IOException("offline")) else Result.success(hasNextPage)
    }

    override suspend fun refreshAllSpots(): Result<Unit> = Result.success(Unit)

    override suspend fun refreshSpot(id: String): Result<Unit> = Result.success(Unit)

    override suspend fun refreshReviews(spotId: String): Result<Unit> = Result.success(Unit)

    override suspend fun addReview(spotId: String, stars: Int, text: String): Result<Unit> = Result.success(Unit)

    override suspend fun editSpot(id: String, name: String, description: String, openNow: Boolean) {
        spots.value = spots.value.map { if (it.id == id) it.copy(name = name, description = description, openNow = openNow) else it }
    }

    companion object {
        private fun spot(id: String, name: String, category: Category) = Spot(
            id = id,
            name = name,
            category = category,
            rating = 4.0,
            priceLevel = 1,
            lat = 44.427,
            lng = 26.103,
            openNow = true,
            photoUrl = "",
            description = "",
            updatedAt = 1_700_000_000_000
        )

        val sampleSpots = listOf(
            spot("spot-1", "Central Canteen", Category.CANTEEN),
            spot("spot-2", "Espresso Lab", Category.CAFE),
            spot("spot-3", "Pizza Stop", Category.FASTFOOD)
        )
    }
}
