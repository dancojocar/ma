package com.unieats.app.data.remote

import com.unieats.app.data.auth.AuthTokens
import com.unieats.shared.DescribeRequest
import com.unieats.shared.Spot
import io.ktor.client.HttpClient
import io.ktor.client.plugins.retry
import io.ktor.client.plugins.timeout
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.LineEnding
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

class AiStreamException(message: String) : Exception(message)

@Serializable
private data class Delta(val delta: String)

@Serializable
private data class StreamError(val error: ErrorBody) {
    @Serializable
    data class ErrorBody(val code: String = "", val message: String = "")
}

/**
 * POST /api/ai/describe answers with Server-Sent Events. The flow emits each text delta as it
 * arrives, completes on `event: done` and throws [AiStreamException] on `event: error`.
 */
@Singleton
class AiDescribeClient @Inject constructor(
    private val http: HttpClient,
    private val tokens: AuthTokens,
    private val json: Json
) {
    fun describe(spot: Spot): Flow<String> = flow {
        http.preparePost("ai/describe") {
            tokens.token?.let { bearerAuth(it) }
            contentType(ContentType.Application.Json)
            accept(ContentType.Text.EventStream)
            setBody(DescribeRequest(spot))
            // A retry after half the text arrived would repeat it; a model can stream for longer
            // than the default 15 s request timeout.
            retry { noRetry() }
            timeout { requestTimeoutMillis = 120_000 }
        }.execute { response ->
            val channel = response.bodyAsChannel()
            val parser = ServerSentEventParser()
            while (true) {
                // SSE allows CR, LF or CRLF line endings; readLine's default accepts only LF/CRLF.
                val line = channel.readLine(LineEnding.Lenient)
                val event = if (line == null) parser.finish() else parser.accept(line)
                if (event != null) {
                    when (event.event) {
                        "message" -> emit(json.decodeFromString<Delta>(event.data).delta)
                        "done" -> return@execute
                        "error" -> throw AiStreamException(json.decodeFromString<StreamError>(event.data).error.message)
                    }
                }
                if (line == null) return@execute
            }
        }
    }
}
