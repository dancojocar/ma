package com.unieats.app.data.repository

import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.model.SpotsPage
import com.unieats.app.data.remote.ApiClient
import com.unieats.app.data.remote.apiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotRepository @Inject constructor(private val api: ApiClient) {

    suspend fun fetchSpots(page: Int, query: String, category: Category?): Result<SpotsPage> =
        apiCall { api.listSpots(page, query, category) }

    suspend fun fetchSpot(id: String): Result<Spot> = apiCall { api.getSpot(id) }

    suspend fun fetchReviews(spotId: String): Result<List<Review>> = apiCall { api.getReviews(spotId) }
}
