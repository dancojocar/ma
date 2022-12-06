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
class UserTokenEncryptor @Inject constructor(
  private val preferencesEditor: PreferencesEditor,
  @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
  suspend fun encrypt(token: UserToken, cipher: Cipher) {
    withContext(ioDispatcher) {
      with(preferencesEditor) {
        saveUserToken(cipher.doFinal(token.value.toByteArray(Charset.forName("UTF-8"))))
        saveInitialisationVector(cipher.iv)
      }
    }
  }
}