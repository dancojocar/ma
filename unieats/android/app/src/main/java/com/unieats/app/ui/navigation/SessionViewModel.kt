package com.unieats.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val isSignedIn: StateFlow<Boolean> = authRepository.session
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, authRepository.session.value != null)

    fun logout() = authRepository.logout()
}
