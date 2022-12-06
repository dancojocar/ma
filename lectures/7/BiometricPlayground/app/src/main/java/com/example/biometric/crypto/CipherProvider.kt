package com.example.biometric.crypto

import com.example.biometric.crypto.SecretKeyGenerator.Companion.ENCRYPTION_ALGORITHM
import com.example.biometric.crypto.SecretKeyGenerator.Companion.ENCRYPTION_BLOCK_MODE
import com.example.biometric.crypto.SecretKeyGenerator.Companion.ENCRYPTION_PADDING
import com.example.biometric.data.PreferencesEditor
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

class CipherProvider @Inject constructor(
  private val keyGenerator: SecretKeyGenerator,
  private val preferencesEditor: PreferencesEditor,
) {

  private fun getCipher(): Cipher {
    return Cipher.getInstance(TRANSFORMATION)
  }

  fun provideCipherForEncryption(): Cipher = getCipher().apply {
    init(Cipher.ENCRYPT_MODE, keyGenerator.getOrCreateSecretKey())
  }

  suspend fun provideCipherForDecryption(): Cipher {
    val initialisationVector = preferencesEditor.getInitialisationVector()
    return getCipher().apply {
      init(
        Cipher.DECRYPT_MODE,
        keyGenerator.getOrCreateSecretKey(),
        GCMParameterSpec(128, initialisationVector)
      )
    }
  }

  companion object {
    private const val TRANSFORMATION =
      "${ENCRYPTION_ALGORITHM}/${ENCRYPTION_BLOCK_MODE}/${ENCRYPTION_PADDING}"
  }
}