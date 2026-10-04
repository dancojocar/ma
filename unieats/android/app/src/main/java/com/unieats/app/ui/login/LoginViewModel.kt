package com.unieats.app.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, errorMessage = null) }

    fun onPasswordChange(password: String) = _uiState.update { it.copy(password = password, errorMessage = null) }

    /** On success the session appears in TokenStore and the NavGraph leaves this screen. */
    fun login() {
        val state = _uiState.value
        if (!state.canSubmit) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.login(state.email, state.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, password = "") } }
                .onFailure { error ->
                    val message = if ((error as? ClientRequestException)?.response?.status == HttpStatusCode.Unauthorized) {
                        "Wrong email or password."
                    } else {
                        error.toUserMessage()
                    }
                    _uiState.update { it.copy(isLoading = false, errorMessage = message) }
                }
        }
    }
}
