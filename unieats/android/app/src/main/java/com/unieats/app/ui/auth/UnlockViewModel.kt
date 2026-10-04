package com.unieats.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.auth.SessionState
import com.unieats.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.crypto.Cipher
import javax.inject.Inject

@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val displayName: StateFlow<String> = authRepository.session
        .filterIsInstance<SessionState.Locked>()
        .map { it.displayName }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    /**
     * [authenticate] shows the biometric prompt. On success the session turns SignedIn and the
     * NavGraph leaves this screen; a changed enrollment signs out instead.
     */
    suspend fun unlock(authenticate: suspend (Cipher) -> Cipher?) {
        val cipher = authRepository.unlockCipher() ?: return
        val authenticated = authenticate(cipher) ?: return
        authRepository.completeUnlock(authenticated)
    }
}
