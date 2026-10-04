// security-crypto 1.1.0 deprecates EncryptedSharedPreferences/MasterKey without a Jetpack
// replacement; they remain the simplest correct way to keep a token encrypted at rest.
@file:Suppress("DEPRECATION")

package com.unieats.app.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class Session(val token: String, val displayName: String)

/**
 * The JWT lives in EncryptedSharedPreferences: values are AES-256-GCM encrypted with a key held
 * in the Android Keystore, so a copied prefs file is useless without the device.
 */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext context: Context) : AuthTokens {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "unieats_session",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _session = MutableStateFlow(readSession())
    val session: StateFlow<Session?> = _session.asStateFlow()

    override val token: String? get() = _session.value?.token

    fun save(session: Session) {
        prefs.edit {
            putString(KEY_TOKEN, session.token)
            putString(KEY_NAME, session.displayName)
        }
        _session.value = session
    }

    override fun clear() {
        prefs.edit { clear() }
        _session.value = null
    }

    private fun readSession(): Session? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        return Session(token, prefs.getString(KEY_NAME, null).orEmpty())
    }

    private companion object {
        const val KEY_TOKEN = "jwt"
        const val KEY_NAME = "display_name"
    }
}
