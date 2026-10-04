package com.unieats.app.testing

import com.unieats.shared.ConflictResponse
import com.unieats.shared.Spot
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.json.Json

/**
 * A Ktor [MockEngine] that records every request and answers each one with the next queued
 * handler, so a test can script "the first PATCH gets a 409, the second a 200".
 */
class FakeServer(private val json: Json) {

    data class Request(
        val method: HttpMethod,
        val path: String,
        val body: String,
        val idempotencyKey: String?,
        val authorization: String?
    )

    inner class Responses(private val scope: MockRequestHandleScope) {
        fun respondJson(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
            scope.respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

        fun spot(spot: Spot) = respondJson(this@FakeServer.json.encodeToString(spot))

        fun conflict(server: Spot) =
            respondJson(this@FakeServer.json.encodeToString(ConflictResponse(server)), HttpStatusCode.Conflict)

        fun status(status: HttpStatusCode) = respondJson("""{"error":"${status.description}"}""", status)
    }

    val requests = mutableListOf<Request>()
    private val handlers = ArrayDeque<suspend Responses.(Request) -> HttpResponseData>()

    val engine = MockEngine { data ->
        val request = Request(
            method = data.method,
            path = data.url.encodedPath,
            body = data.body.toByteArray().decodeToString(),
            idempotencyKey = data.headers["Idempotency-Key"],
            authorization = data.headers[HttpHeaders.Authorization]
        )
        requests += request
        val handler = handlers.removeFirstOrNull()
            ?: error("Unexpected request: ${request.method.value} ${request.path}")
        Responses(this).handler(request)
    }

    fun enqueue(handler: suspend Responses.(Request) -> HttpResponseData) {
        handlers += handler
    }
}
