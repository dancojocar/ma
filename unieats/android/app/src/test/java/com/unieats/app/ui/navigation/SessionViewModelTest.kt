package com.unieats.app.ui.navigation

import com.unieats.app.data.repository.AuthRepository
import com.unieats.app.testing.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import javax.crypto.Cipher

class SessionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val enableCipher: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val authRepository = mockk<AuthRepository>(relaxed = true) {
        every { biometricEnableCipher() } returns enableCipher
    }
    private val viewModel = SessionViewModel(authRepository)

    @Test
    fun `the cipher the prompt authenticated turns biometric unlock on`() = runTest {
        val authenticated: Cipher = Cipher.getInstance("AES/GCM/NoPadding")
        var prompted: Cipher? = null

        viewModel.enableBiometric { cipher -> prompted = cipher; authenticated }

        assertSame(enableCipher, prompted)
        coVerify { authRepository.enableBiometric(authenticated) }
    }

    @Test
    fun `a cancelled prompt leaves biometric unlock off`() = runTest {
        viewModel.enableBiometric { null }

        coVerify(exactly = 0) { authRepository.enableBiometric(any()) }
    }

    @Test
    fun `without a usable key there is no prompt`() = runTest {
        every { authRepository.biometricEnableCipher() } returns null
        var prompted = false

        viewModel.enableBiometric { prompted = true; it }

        assertFalse(prompted)
        coVerify(exactly = 0) { authRepository.enableBiometric(any()) }
    }

    @Test
    fun `turning it off and signing out go to the repository`() = runTest {
        viewModel.disableBiometric()
        viewModel.logout()

        coVerify { authRepository.disableBiometric() }
        coVerify { authRepository.logout() }
    }
}
