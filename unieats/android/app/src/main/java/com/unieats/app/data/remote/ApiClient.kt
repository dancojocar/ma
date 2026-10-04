package com.unieats.app.data.remote

import com.unieats.app.data.auth.AuthTokens
import com.unieats.app.data.model.Category
import com.unieats.app.data.model.LoginRequest
import com.unieats.app.data.model.LoginResponse
import com.unieats.app.data.model.NewReview
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.model.SpotPatch
import com.unieats.app.data.model.SpotsPage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

const val PAGE_SIZE = 20

@Singleton
class ApiClient @Inject constructor(
    private val http: HttpClient,
    private val tokens: AuthTokens
) {

    suspend fun login(email: String, password: String): LoginResponse =
        http.post("auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun listSpots(page: Int, query: String, category: Category?): SpotsPage =
        http.get("spots") {
            parameter("page", page)
            parameter("limit", PAGE_SIZE)
            if (query.isNotBlank()) parameter("q", query.trim())
            if (category != null) parameter("category", category.apiValue)
        }.body()

    suspend fun getSpot(id: String): Spot = http.get("spots/$id").body()

    suspend fun getReviews(spotId: String): List<Review> = http.get("spots/$spotId/reviews").body()

    suspend fun patchSpot(id: String, patch: SpotPatch, idempotencyKey: String): Spot =
        http.patch("spots/$id") {
            authorized()
            header("Idempotency-Key", idempotencyKey)
            contentType(ContentType.Application.Json)
            setBody(patch)
        }.body()

    suspend fun addReview(spotId: String, review: NewReview): Review =
        http.post("spots/$spotId/reviews") {
            authorized()
            header("Idempotency-Key", UUID.randomUUID().toString())
            contentType(ContentType.Application.Json)
            setBody(review)
        }.body()

    /** Every mutation carries the JWT; reads stay anonymous. */
    private fun HttpRequestBuilder.authorized() {
        tokens.token?.let { bearerAuth(it) }
    }
}
