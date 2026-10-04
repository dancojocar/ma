package com.unieats.app.data.sync

import com.unieats.app.data.local.toDomain
import com.unieats.app.testing.SyncFixture
import com.unieats.app.testing.SyncFixture.Companion.pizzaStop
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OutboxSyncTest {

    private lateinit var fixture: SyncFixture

    private fun runSyncTest(block: suspend SyncFixture.() -> Unit) = runTest {
        fixture = SyncFixture(backgroundScope)
        fixture.seed(pizzaStop)
        fixture.block()
    }

    @After
    fun closeDatabase() = fixture.close()

    @Test
    fun `edits made before a replay are merged into one queued op`() = runSyncTest {
        repository.editSpot(pizzaStop.id, "Pizza Stop", "First try", openNow = true)
        repository.editSpot(pizzaStop.id, "Pizza Stop", "Second try", openNow = false)

        val queued = outbox().single()
        assertEquals("Second try", patchOf(queued).description)
        assertEquals(pizzaStop.updatedAt, patchOf(queued).updatedAt)
        assertEquals(SyncFixture.USER_1, queued.userId)
        assertTrue(row(pizzaStop.id)!!.pendingSync)
    }

    @Test
    fun `an edit made while the PATCH is in flight is kept and rebased onto the server version`() = runSyncTest {
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        val sent = outbox().single()
        server.enqueue { request ->
            repository.editSpot(pizzaStop.id, "Pizza Stop (late night)", pizzaStop.description, openNow = true)
            spot(pizzaStop.copy(name = patchOf(request).name, updatedAt = 2_000))
        }

        assertEquals(ReplayResult.Done, outboxSync.replay())

        assertEquals("Pizza Stop (new oven)", patchOf(server.requests.single()).name)
        assertEquals(sent.opId, server.requests.single().idempotencyKey)
        val row = row(pizzaStop.id)!!
        assertEquals("Pizza Stop (late night)", row.name)
        assertTrue(row.pendingSync)
        assertEquals(2_000L, row.updatedAt)
        val newer = outbox().single()
        assertNotEquals(sent.opId, newer.opId)
        assertFalse(newer.attempted)
        assertEquals("Pizza Stop (late night)", patchOf(newer).name)
        assertEquals(2_000L, patchOf(newer).updatedAt)

        server.enqueue { request -> spot(pizzaStop.copy(name = patchOf(request).name, updatedAt = 3_000)) }
        assertEquals(ReplayResult.Done, outboxSync.replay())

        assertEquals(2_000L, patchOf(server.requests.last()).updatedAt)
        assertEquals(newer.opId, server.requests.last().idempotencyKey)
        assertEquals(pizzaStop.copy(name = "Pizza Stop (late night)", updatedAt = 3_000), row(pizzaStop.id)!!.toDomain())
        assertFalse(row(pizzaStop.id)!!.pendingSync)
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun `the answer for an op that was discarded while in flight is not written back`() = runSyncTest {
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        server.enqueue { request ->
            outboxSync.discardAll()
            spot(pizzaStop.copy(name = patchOf(request).name, updatedAt = 2_000))
        }

        assertEquals(ReplayResult.Done, outboxSync.replay())

        assertTrue(outbox().isEmpty())
        assertNull(row(pizzaStop.id))
    }

    @Test
    fun `a 409 means the server wins and the op is dropped`() = runSyncTest {
        val serverCopy = pizzaStop.copy(name = "Pizza Stop (renamed by staff)", updatedAt = 5_000)
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        server.enqueue { conflict(serverCopy) }

        assertEquals(ReplayResult.Done, outboxSync.replay())

        assertEquals(1, server.requests.size)
        assertEquals(serverCopy, row(pizzaStop.id)!!.toDomain())
        assertFalse(row(pizzaStop.id)!!.pendingSync)
        assertTrue(outbox().isEmpty())
    }

    @Test
    fun `a 401 ends the session but keeps the queued edit`() = runSyncTest {
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        val queued = outbox().single()
        server.enqueue { status(HttpStatusCode.Unauthorized) }

        assertEquals(ReplayResult.NeedsLogin, outboxSync.replay())

        assertEquals("Bearer jwt-${SyncFixture.USER_1}", server.requests.single().authorization)
        coVerify { tokens.clear() }
        assertEquals(queued.opId, outbox().single().opId)
        assertEquals("Pizza Stop (new oven)", row(pizzaStop.id)!!.name)
        assertTrue(row(pizzaStop.id)!!.pendingSync)
    }

    @Test
    fun `nothing is sent without a session`() = runSyncTest {
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        userId = null

        assertEquals(ReplayResult.NeedsLogin, outboxSync.replay())

        assertTrue(server.requests.none { it.method == HttpMethod.Patch })
        assertEquals(1, outbox().size)
    }
}
