package com.unieats.app.data.remote

import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import java.io.IOException

suspend fun <T> apiCall(block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

fun Throwable.toUserMessage(): String = when (this) {
    is AiStreamException -> "The AI could not finish: $message"
    is HttpRequestTimeoutException -> "The server took too long to answer."
    is ResponseException -> when (response.status.value) {
        404 -> "Not found on the server."
        429 -> "Too many requests. Try again in a minute."
        else -> "The server answered ${response.status.value}. Try again."
    }
    is SerializationException -> "The server sent a response the app could not read."
    is IOException -> "Can't reach the server. Is it running on port 3000?"
    else -> "Something went wrong (${this::class.simpleName})."
}
