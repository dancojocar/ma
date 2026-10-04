package com.unieats.app.data.remote

import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.model.SpotsPage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject
import javax.inject.Singleton

const val PAGE_SIZE = 20

@Singleton
class ApiClient @Inject constructor(private val http: HttpClient) {

    suspend fun listSpots(page: Int, query: String, category: Category?): SpotsPage =
        http.get("spots") {
            parameter("page", page)
            parameter("limit", PAGE_SIZE)
            if (query.isNotBlank()) parameter("q", query.trim())
            if (category != null) parameter("category", category.apiValue)
        }.body()

    suspend fun getSpot(id: String): Spot = http.get("spots/$id").body()

    suspend fun getReviews(spotId: String): List<Review> = http.get("spots/$spotId/reviews").body()
}
