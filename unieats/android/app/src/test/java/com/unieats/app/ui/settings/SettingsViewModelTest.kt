package com.unieats.app.ui.settings

import app.cash.turbine.test
import com.unieats.app.cloud.CrashConsent
import com.unieats.app.cloud.CrashReporter
import com.unieats.app.cloud.RemoteConfig
import com.unieats.app.testing.MainDispatcherRule
import com.unieats.shared.RemoteFlags
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val flags = MutableStateFlow(RemoteFlags())
    private val consentGranted = MutableStateFlow(false)

    private val remoteConfig = mockk<RemoteConfig> { every { flags } returns this@SettingsViewModelTest.flags }
    private val crashConsent = mockk<CrashConsent>(relaxUnitFun = true) {
        every { granted } returns consentGranted
        every { isGranted } answers { consentGranted.value }
    }
    private val crashReporter = mockk<CrashReporter>(relaxed = true)

    private fun viewModel() = SettingsViewModel(remoteConfig, crashConsent, crashReporter)

    @Test
    fun `fetch and activate shows the new flag value`() = runTest {
        coEvery { remoteConfig.fetchAndActivate() } coAnswers {
            flags.value = RemoteFlags(showNewRatingUi = true)
            Result.success(flags.value)
        }
        val viewModel = viewModel()

        viewModel.uiState.test {
            assertFalse(awaitItem().showNewRatingUi)
            viewModel.fetchAndActivate()
            val activated = expectMostRecentItem()
            assertTrue(activated.showNewRatingUi)
            assertEquals("Activated: show_new_rating_ui = true", activated.message)
        }
    }

    @Test
    fun `a failed fetch keeps the current flags`() = runTest {
        coEvery { remoteConfig.fetchAndActivate() } returns Result.failure(IOException("offline"))
        val viewModel = viewModel()

        viewModel.uiState.test {
            skipItems(1)
            viewModel.fetchAndActivate()
            val state = expectMostRecentItem()
            assertFalse(state.showNewRatingUi)
            assertTrue(state.message!!.startsWith("Kept current flags."))
        }
    }

    @Test
    fun `test crash is handed to the crash reporter`() = runTest {
        consentGranted.value = true
        viewModel().recordTestCrash()

        verify(exactly = 1) { crashReporter.recordError(match { it.message == "Test crash from the Settings screen" }) }
    }

    @Test
    fun `the consent switch is persisted through CrashConsent`() = runTest {
        viewModel().setCrashReporting(true)

        verify { crashConsent.set(true) }
    }
}
