package com.unieats.app.ui.auth

import app.cash.turbine.test
import com.unieats.app.data.auth.Session
import com.unieats.app.data.auth.SessionState
import com.unieats.app.data.repository.AuthRepository
import com.unieats.app.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import javax.crypto.Cipher

class UnlockViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = MutableStateFlow<SessionState>(SessionState.Locked("Student"))
    private val unlockCipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val authRepository = mockk<AuthRepository>(relaxed = true) {
        every { session } returns this@UnlockViewModelTest.session
        coEvery { unlockCipher() } returns unlockCipher
    }

    @Test
    fun `the unlock screen greets the locked user`() = runTest {
        UnlockViewModel(authRepository).displayName.test {
            assertEquals("Student", expectMostRecentItem())
            session.value = SessionState.SignedIn(Session("jwt", "Student", "user-1"))
            expectNoEvents()
        }
    }

    @Test
    fun `an authenticated cipher completes the unlock`() = runTest {
        val authenticated: Cipher = Cipher.getInstance("AES/GCM/NoPadding")

        UnlockViewModel(authRepository).unlock { authenticated }

        coVerify { authRepository.completeUnlock(authenticated) }
    }

    @Test
    fun `a cancelled prompt stays locked`() = runTest {
        UnlockViewModel(authRepository).unlock { null }

        coVerify(exactly = 0) { authRepository.completeUnlock(any()) }
    }

    @Test
    fun `no prompt once the key was invalidated`() = runTest {
        coEvery { authRepository.unlockCipher() } returns null
        var prompted = false

        UnlockViewModel(authRepository).unlock { prompted = true; it }

        assertEquals(false, prompted)
        coVerify(exactly = 0) { authRepository.completeUnlock(any()) }
    }
}
