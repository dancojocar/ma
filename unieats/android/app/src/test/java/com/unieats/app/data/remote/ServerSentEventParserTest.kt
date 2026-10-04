package com.unieats.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerSentEventParserTest {

    private fun parse(vararg lines: String): List<ServerSentEvent> {
        val parser = ServerSentEventParser()
        return lines.mapNotNull(parser::accept) + listOfNotNull(parser.finish())
    }

    @Test
    fun `data frames without an event name are messages`() {
        val events = parse("""data: {"delta":"Crispy "}""", "", """data: {"delta":"crust."}""", "")
        assertEquals(
            listOf(
                ServerSentEvent("message", """{"delta":"Crispy "}"""),
                ServerSentEvent("message", """{"delta":"crust."}""")
            ),
            events
        )
    }

    @Test
    fun `named events such as done keep their name and reset afterwards`() {
        val events = parse("event: done", """data: {"source":"template"}""", "", "data: next", "")
        assertEquals(ServerSentEvent("done", """{"source":"template"}"""), events[0])
        assertEquals(ServerSentEvent("message", "next"), events[1])
    }

    @Test
    fun `a frame cut off without a blank line is still delivered by finish`() {
        val parser = ServerSentEventParser()
        assertNull(parser.accept("event: error"))
        assertNull(parser.accept("""data: {"error":{"code":"ai_unavailable"}}"""))
        assertEquals(ServerSentEvent("error", """{"error":{"code":"ai_unavailable"}}"""), parser.finish())
    }

    @Test
    fun `comments are ignored and multi-line data is joined`() {
        assertEquals(listOf(ServerSentEvent("message", "a\nb")), parse(": keep-alive", "data: a", "data: b", ""))
    }
}
