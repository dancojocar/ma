package com.example.biometric.feature.login

import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometric.crypto.CipherProvider
import com.example.biometric.data.AuthRepository
import com.example.biometric.data.model.Password
import com.example.biometric.data.model.Username
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cipherProvider: CipherProvider,
    @Named("canUseBiometrics") val canUseBiometrics: Boolean
) : ViewModel() {
    private val loginActionFlow = MutableSharedFlow<LoginAction>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val loginState = loginActionFlow
        .flatMapLatest { action ->
            when (action) {
                is LoginAction.Encrypt -> encryptTokenFlow(action.authenticationResult.cryptoObject?.cipher)
                is LoginAction.Login -> getUserTokenFlow(action.username, action.password)
                is LoginAction.Decrypt -> decryptTokenFlow(action.authenticationResult.cryptoObject?.cipher)
                LoginAction.ShowLoginBiometrics -> showDecryptionBiometricsFlow()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, LoginState.Idle)

    private suspend fun showDecryptionBiometricsFlow() =
        if (authRepository.isBiometricSetUp()) {
            flow { emit(cipherProvider.provideCipherForDecryption()) }
                .map<Cipher, LoginState> {
                    LoginState.ShowBiometrics.Decryption(BiometricPrompt.CryptoObject(it))
                }
                .catch { emit(LoginState.Idle) }
        } else {
            flowOf(LoginState.Idle)
        }

    private fun decryptTokenFlow(cipher: Cipher?) =
        authFlow(
            cipher = cipher,
            action = authRepository::decryptToken
        )

    private fun encryptTokenFlow(cipher: Cipher?) =
        authFlow(
            cipher = cipher,
            action = authRepository::encryptToken
        )

    private inline fun authFlow(cipher: Cipher?, crossinline action: suspend (Cipher) -> Unit) =
        if (cipher != null) {
            flow { emit(action(cipher)) }
                .map<Unit, LoginState> { LoginState.Success }
                .onStart { emit(LoginState.Loading) }
        } else {
            flowOf(LoginState.Failure("No cipher defined"))
        }

    private fun getUserTokenFlow(username: Username, password: Password) =
        flow { emit(authRepository.login(username, password)) }
            .map {
                if (canUseBiometrics) {
                    LoginState.ShowBiometrics.Encryption(
                        BiometricPrompt.CryptoObject(
                            cipherProvider.provideCipherForEncryption()
                        )
                    )
                } else {
                    LoginState.Success
                }
            }
            .catch { emit(LoginState.Failure(it.message)) }
            .onStart { emit(LoginState.Loading) }

    fun triggerLogin(username: Username, password: Password) {
        viewModelScope.launch {
            loginActionFlow.emit(LoginAction.Login(username, password))
        }
    }

    fun triggerEncryption(authResult: BiometricPrompt.AuthenticationResult) {
        viewModelScope.launch {
            loginActionFlow.emit(LoginAction.Encrypt(authResult))
        }
    }

    fun triggerDecryption(authResult: BiometricPrompt.AuthenticationResult) {
        viewModelScope.launch {
            loginActionFlow.emit(LoginAction.Decrypt(authResult))
        }
    }

    fun onResume() {
        viewModelScope.launch {
            loginActionFlow.emit(LoginAction.ShowLoginBiometrics)
        }
    }
}

sealed class LoginAction {
    data class Login(val username: Username, val password: Password) : LoginAction()
    data class Encrypt(
        val authenticationResult: BiometricPrompt.AuthenticationResult
    ) : LoginAction()

    data class Decrypt(
        val authenticationResult: BiometricPrompt.AuthenticationResult
    ) : LoginAction()

    object ShowLoginBiometrics : LoginAction()
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Failure(val errorMessage: String?) : LoginState()

    sealed class ShowBiometrics : LoginState() {
        abstract val cryptoObject: BiometricPrompt.CryptoObject

        data class Encryption(
            override val cryptoObject: BiometricPrompt.CryptoObject
        ) : ShowBiometrics()

        data class Decryption(
            override val cryptoObject: BiometricPrompt.CryptoObject
        ) : ShowBiometrics()
    }
}