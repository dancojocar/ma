package com.unieats.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.auth.SessionState
import com.unieats.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val session: StateFlow<SessionState> = authRepository.session
    val biometricEnabled: StateFlow<Boolean> = authRepository.biometricEnabled

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    /** [authenticate] shows the biometric prompt and returns the cipher it unlocked, or null. */
    suspend fun enableBiometric(authenticate: suspend (Cipher) -> Cipher?) {
        val cipher = authRepository.biometricEnableCipher() ?: return
        val authenticated = authenticate(cipher) ?: return
        authRepository.enableBiometric(authenticated)
    }

    fun disableBiometric() {
        viewModelScope.launch { authRepository.disableBiometric() }
    }
}
