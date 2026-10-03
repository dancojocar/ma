package com.unieats.app.data.remote

/** One Server-Sent Event: the `event:` name ("message" when absent) and its joined `data:` lines. */
data class ServerSentEvent(val event: String, val data: String)

/**
 * Turns `text/event-stream` lines into events. Frames end with a blank line; `data:` lines in one
 * frame are joined with "\n"; comment lines (":") are ignored.
 */
class ServerSentEventParser {
    private var event = DEFAULT_EVENT
    private val data = mutableListOf<String>()

    /** Feeds one line; returns an event when this line completes a frame. */
    fun accept(line: String): ServerSentEvent? {
        when {
            line.isEmpty() -> return flush()
            line.startsWith(":") -> Unit
            line.startsWith("event:") -> event = line.removePrefix("event:").trim()
            line.startsWith("data:") -> data += line.removePrefix("data:").removePrefix(" ")
        }
        return null
    }

    /** Emits a frame the server did not terminate with a blank line before closing. */
    fun finish(): ServerSentEvent? = flush()

    private fun flush(): ServerSentEvent? {
        val complete = if (data.isEmpty()) null else ServerSentEvent(event, data.joinToString("\n"))
        event = DEFAULT_EVENT
        data.clear()
        return complete
    }

    private companion object {
        const val DEFAULT_EVENT = "message"
    }
}
