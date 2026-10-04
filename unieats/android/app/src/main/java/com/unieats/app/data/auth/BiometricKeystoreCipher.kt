package com.unieats.app.data.auth

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * AES-256-GCM like [KeystoreCipher], but the Keystore only lets the key work inside a [Cipher]
 * that a strong biometric check (BiometricPrompt with a CryptoObject) has just authenticated:
 * every encrypt and every decrypt needs its own prompt. Enrolling another fingerprint or face
 * invalidates the key for good.
 *
 * Usage: get a cipher here, pass it to the prompt, then [seal] or [open] with the cipher the
 * prompt hands back.
 */
class BiometricKeystoreCipher(private val alias: String) {

    /** Replaces any old key with a new one bound to the biometrics enrolled right now. */
    @RequiresApi(Build.VERSION_CODES.R)
    fun encryptCipher(): Cipher {
        deleteKey()
        return Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, createKey()) }
    }

    /** Throws [KeyPermanentlyInvalidatedException] once the enrolled biometrics have changed. */
    fun decryptCipher(sealed: String): Cipher {
        val key = existingKey() ?: throw KeyPermanentlyInvalidatedException("Biometric key is gone")
        val iv = Base64.decode(sealed, Base64.NO_WRAP).copyOfRange(0, IV_BYTES)
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        }
    }

    fun seal(authenticated: Cipher, plaintext: String): String {
        val sealed = authenticated.iv + authenticated.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(sealed, Base64.NO_WRAP)
    }

    fun open(authenticated: Cipher, sealed: String): String? = try {
        val bytes = Base64.decode(sealed, Base64.NO_WRAP)
        String(authenticated.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
    } catch (e: GeneralSecurityException) {
        null
    }

    fun deleteKey() {
        keyStore().deleteEntry(alias)
    }

    private fun existingKey(): SecretKey? =
        (keyStore().getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey

    @RequiresApi(Build.VERSION_CODES.R)
    private fun createKey(): SecretKey {
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(true)
            // 0 seconds: no grace period, each use of the key needs a fresh prompt.
            .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            .setInvalidatedByBiometricEnrollment(true)
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            .apply { init(spec) }
            .generateKey()
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
