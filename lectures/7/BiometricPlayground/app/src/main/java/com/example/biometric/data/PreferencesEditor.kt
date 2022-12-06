package com.example.biometric.data

import android.content.SharedPreferences
import com.example.biometric.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class PreferencesEditor @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val sharedPreferences: SharedPreferences
) {
    suspend fun saveUserToken(encryptedToken: ByteArray) {
        withContext(ioDispatcher) {
            sharedPreferences.edit()
                .putString(TOKEN_KEY, String(encryptedToken, Charsets.ISO_8859_1))
                .apply()
        }
    }

    suspend fun saveInitialisationVector(vector: ByteArray) {
        withContext(ioDispatcher) {
            sharedPreferences.edit()
                .putString(INITIALISATION_VECTOR_KEY, String(vector, Charsets.ISO_8859_1))
                .apply()
        }
    }

    suspend fun getUserToken(): ByteArray = withContext(ioDispatcher) {
        sharedPreferences.getString(TOKEN_KEY, "").orEmpty().toByteArray(Charsets.ISO_8859_1)
    }

    suspend fun getInitialisationVector(): ByteArray = withContext(ioDispatcher) {
        sharedPreferences.getString(INITIALISATION_VECTOR_KEY, "")
            .orEmpty()
            .toByteArray(Charsets.ISO_8859_1)
    }

    suspend fun isBiometricSetUp(): Boolean = withContext(ioDispatcher) {
        sharedPreferences.contains(INITIALISATION_VECTOR_KEY)

    }
}

private const val TOKEN_KEY = "token"
private const val INITIALISATION_VECTOR_KEY = "vector"