package com.example.biometric.data

import com.example.biometric.crypto.UserTokenDecryptor
import com.example.biometric.crypto.UserTokenEncryptor
import com.example.biometric.data.model.Password
import com.example.biometric.data.model.UserToken
import com.example.biometric.data.model.Username
import com.example.biometric.data.remote.AuthApi
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val preferencesEditor: PreferencesEditor,
    private val userTokenDecryptor: UserTokenDecryptor,
    private val userTokenEncryptor: UserTokenEncryptor
) {
    private var userToken: UserToken? = null

    suspend fun <Output> callWithAuthToken(request: suspend (UserToken) -> Output): Output =
        userToken?.let { request(it) }
            ?: throw IllegalStateException("No token")

    suspend fun login(username: Username, password: Password): UserToken =
        authApi.login(username.value, password.value).also(::userToken::set)

    suspend fun encryptToken(cipher: Cipher) {
        userToken?.let { userTokenEncryptor.encrypt(it, cipher) }
            ?: throw IllegalStateException("No token")
    }

    suspend fun decryptToken(cipher: Cipher) {
        userToken = userTokenDecryptor.decrypt(cipher)
    }

    suspend fun isBiometricSetUp(): Boolean = preferencesEditor.isBiometricSetUp()
}