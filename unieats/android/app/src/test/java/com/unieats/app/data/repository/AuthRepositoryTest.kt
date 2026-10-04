package com.unieats.app.data.repository

import com.unieats.app.data.auth.Session
import com.unieats.app.data.auth.TokenStore
import com.unieats.app.data.local.OutboxEntity
import com.unieats.app.data.local.OutboxType
import com.unieats.app.data.local.toEntity
import com.unieats.app.testing.SyncFixture
import com.unieats.app.testing.SyncFixture.Companion.USER_1
import com.unieats.app.testing.SyncFixture.Companion.USER_2
import com.unieats.app.testing.SyncFixture.Companion.espressoLab
import com.unieats.app.testing.SyncFixture.Companion.pizzaStop
import com.unieats.shared.LoginResponse
import com.unieats.shared.SpotPatch
import com.unieats.shared.User
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryTest {

    private lateinit var fixture: SyncFixture
    private val tokenStore = mockk<TokenStore>(relaxed = true)

    private fun runAuthTest(block: suspend SyncFixture.(AuthRepository) -> Unit) = runTest {
        fixture = SyncFixture(backgroundScope)
        fixture.seed(pizzaStop, espressoLab)
        fixture.block(AuthRepository(fixture.api, tokenStore, fixture.outboxSync, fixture.syncScheduler))
    }

    @After
    fun closeDatabase() = fixture.close()

    private fun SyncFixture.answerLogin(user: User) = server.enqueue {
        respondJson(json.encodeToString(LoginResponse("jwt-${user.id}", user)))
    }

    @Test
    fun `signing out discards queued edits and the optimistic rows`() = runAuthTest { auth ->
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)

        auth.logout()

        assertTrue(outbox().isEmpty())
        assertNull(row(pizzaStop.id))
        assertEquals(espressoLab.name, row(espressoLab.id)!!.name)
        coVerifyOrder {
            syncScheduler.cancel()
            tokenStore.clear()
        }
    }

    @Test
    fun `after a 401 the same user signing in again keeps their queued edits`() = runAuthTest { auth ->
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        val queued = outbox().single()
        answerLogin(User(USER_1, "student@unieats.app", "Student"))

        assertTrue(auth.login("student@unieats.app", "password").isSuccess)

        assertEquals(queued.opId, outbox().single().opId)
        assertTrue(row(pizzaStop.id)!!.pendingSync)
        coVerify { tokenStore.save(Session("jwt-$USER_1", "Student", USER_1)) }
        verify { syncScheduler.requestSync() }
    }

    @Test
    fun `another user signing in discards edits queued by someone else before the token is saved`() = runAuthTest { auth ->
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        val legacy = queueWithoutOwner()
        var outboxWhenSaved: List<OutboxEntity>? = null
        coEvery { tokenStore.save(any()) } coAnswers { outboxWhenSaved = outbox() }
        answerLogin(User(USER_2, "maria@unieats.app", "Maria"))

        assertTrue(auth.login("maria@unieats.app", "password").isSuccess)

        assertEquals(listOf(legacy.opId), outboxWhenSaved?.map { it.opId })
        assertEquals(listOf(legacy.opId), outbox().map { it.opId })
        assertNull(row(pizzaStop.id))
        assertTrue(row(espressoLab.id)!!.pendingSync)
    }

    @Test
    fun `a failed sign in changes nothing`() = runAuthTest { auth ->
        repository.editSpot(pizzaStop.id, "Pizza Stop (new oven)", pizzaStop.description, openNow = true)
        server.enqueue { status(HttpStatusCode.Unauthorized) }

        assertTrue(auth.login(USER_2, "wrong").isFailure)

        assertEquals(1, outbox().size)
        assertTrue(row(pizzaStop.id)!!.pendingSync)
        coVerify(exactly = 0) { tokenStore.save(any()) }
    }

    /** An op queued at database version 1, before ops recorded who made them. */
    private suspend fun SyncFixture.queueWithoutOwner(): OutboxEntity {
        val edited = espressoLab.copy(name = "Espresso Lab (oat milk)")
        db.spotDao().upsert(edited.toEntity(pendingSync = true))
        val op = OutboxEntity(
            opId = "legacy-op",
            type = OutboxType.UPDATE,
            entityId = edited.id,
            payload = json.encodeToString(SpotPatch(edited.name, edited.description, edited.openNow, edited.updatedAt)),
            createdAt = 0,
            userId = null
        )
        db.outboxDao().insert(op)
        return op
    }
}
