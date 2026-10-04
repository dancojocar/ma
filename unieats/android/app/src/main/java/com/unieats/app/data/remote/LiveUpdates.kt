package com.unieats.app.data.remote

import android.util.Log
import com.unieats.app.data.model.Spot
import com.unieats.app.di.LiveUrl
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.timeout
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

sealed interface LiveEvent {
    data object Connected : LiveEvent
    data object Disconnected : LiveEvent
    data class SpotCreated(val spot: Spot) : LiveEvent
    data class SpotUpdated(val spot: Spot) : LiveEvent
    data class SpotDeleted(val id: String) : LiveEvent
}

@Serializable
private data class LiveMessage(val type: String, val spot: Spot? = null, val id: String? = null)

private const val TAG = "LiveUpdates"
private const val MAX_BACKOFF_MS = 30_000L

@Singleton
class LiveUpdates @Inject constructor(
    private val http: HttpClient,
    private val json: Json,
    @param:LiveUrl private val liveUrl: String
) {

    /**
     * Cold flow: the socket is opened when collection starts and closed when the collector is
     * cancelled. Drops reconnect with exponential back-off (1 s, 2 s, 4 s … 30 s).
     */
    fun events(): Flow<LiveEvent> = channelFlow {
        var attempt = 0
        while (true) {
            try {
                http.webSocket(urlString = liveUrl, request = {
                    timeout { requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS }
                }) {
                    attempt = 0
                    send(LiveEvent.Connected)
                    for (frame in incoming) {
                        if (frame is Frame.Text) handleLiveMessage(frame.readText())
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "live connection failed: ${e.message}")
            }
            send(LiveEvent.Disconnected)
            delay(minOf(1_000L shl attempt.coerceAtMost(5), MAX_BACKOFF_MS))
            attempt++
        }
    }

    private suspend fun ProducerScope<LiveEvent>.handleLiveMessage(text: String) {
        val message = try {
            json.decodeFromString<LiveMessage>(text)
        } catch (e: SerializationException) {
            Log.w(TAG, "ignoring malformed live message: $text", e)
            return
        }
        val event = when (message.type) {
            "spot.created" -> message.spot?.let(LiveEvent::SpotCreated)
            "spot.updated" -> message.spot?.let(LiveEvent::SpotUpdated)
            "spot.deleted" -> message.id?.let(LiveEvent::SpotDeleted)
            else -> null
        }
        if (event != null) send(event)
    }
}
