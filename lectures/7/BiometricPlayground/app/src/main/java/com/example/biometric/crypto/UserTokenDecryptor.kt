package com.example.biometric.crypto

import com.example.biometric.data.PreferencesEditor
import com.example.biometric.data.model.UserToken
import com.example.biometric.di.IoDispatcher
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

@Singleton
class UserTokenDecryptor @Inject constructor(
    private val preferencesEditor: PreferencesEditor,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun decrypt(cipher: Cipher): UserToken = withContext(ioDispatcher) {
        val encryptedToken = preferencesEditor.getUserToken()

        val plaintext = cipher.doFinal(encryptedToken)

        UserToken(String(plaintext, Charset.forName("UTF-8")))
    }
}