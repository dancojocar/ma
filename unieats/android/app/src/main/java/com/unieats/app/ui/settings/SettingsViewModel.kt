package com.unieats.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.cloud.CrashConsent
import com.unieats.app.cloud.CrashReporter
import com.unieats.app.cloud.RemoteConfig
import com.unieats.app.data.remote.toUserMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val showNewRatingUi: Boolean = false,
    val isFetching: Boolean = false,
    val crashReportingEnabled: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val remoteConfig: RemoteConfig,
    private val crashConsent: CrashConsent,
    private val crashReporter: CrashReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())

    val uiState: StateFlow<SettingsUiState> = combine(
        _uiState,
        remoteConfig.flags,
        crashConsent.granted
    ) { state, flags, consent ->
        state.copy(showNewRatingUi = flags.showNewRatingUi, crashReportingEnabled = consent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    fun fetchAndActivate() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetching = true, message = null) }
            remoteConfig.fetchAndActivate()
                .onSuccess { flags ->
                    _uiState.update { it.copy(isFetching = false, message = "Activated: show_new_rating_ui = ${flags.showNewRatingUi}") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isFetching = false, message = "Kept current flags. ${error.toUserMessage()}") }
                }
        }
    }

    fun setCrashReporting(enabled: Boolean) = crashConsent.set(enabled)

    fun recordTestCrash() {
        crashReporter.recordError(IllegalStateException("Test crash from the Settings screen"))
        val message = if (crashConsent.isGranted) {
            "Test crash recorded (Logcat tag CrashReporter)."
        } else {
            "Not recorded: crash reporting is off."
        }
        _uiState.update { it.copy(message = message) }
    }
}
