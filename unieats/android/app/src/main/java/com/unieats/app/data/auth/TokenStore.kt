package com.unieats.app.data.auth

import android.content.Context
import android.os.Build
import android.security.keystore.KeyPermanentlyInvalidatedException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.unieats.app.R
import com.unieats.app.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

data class Session(val token: String, val displayName: String, val userId: String)

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    /** Biometric unlock is on and nobody has unlocked since the app started. */
    data class Locked(val displayName: String) : SessionState
    data class SignedIn(val session: Session) : SessionState
}

/**
 * [unlockedToken] is the token [TokenStore.completeUnlock] opened with a biometric prompt; it
 * lives in memory only, so every cold start is [SessionState.Locked] again.
 */
internal fun sessionStateOf(
    token: String?,
    biometricToken: Boolean,
    unlockedToken: String?,
    displayName: String,
    userId: String
): SessionState = when {
    token != null -> SessionState.SignedIn(Session(token, displayName, userId))
    biometricToken && unlockedToken != null -> SessionState.SignedIn(Session(unlockedToken, displayName, userId))
    biometricToken -> SessionState.Locked(displayName)
    else -> SessionState.SignedOut
}

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * The session lives in Jetpack DataStore, but every value is encrypted by [KeystoreCipher]
 * first, so the file on disk only ever holds ciphertext. With biometric unlock on, the token is
 * sealed by [BiometricKeystoreCipher] instead and can only be read after a biometric prompt.
 */
@Singleton
class TokenStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @ApplicationScope scope: CoroutineScope
) : AuthTokens {

    private val dataStore = context.sessionDataStore
    private val cipher = KeystoreCipher(KEY_ALIAS)
    private val biometricCipher = BiometricKeystoreCipher(BIOMETRIC_KEY_ALIAS)
    private val unlockedToken = MutableStateFlow<String?>(null)

    private val prefs = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val session: StateFlow<SessionState> = combine(prefs, unlockedToken) { stored, unlocked ->
        sessionStateOf(
            token = stored[KEY_TOKEN]?.let(cipher::decrypt),
            biometricToken = KEY_TOKEN_BIOMETRIC in stored,
            unlockedToken = unlocked,
            displayName = stored[KEY_NAME]?.let(cipher::decrypt).orEmpty(),
            userId = stored[KEY_USER_ID]?.let(cipher::decrypt).orEmpty()
        )
    }.stateIn(scope, SharingStarted.Eagerly, SessionState.Loading)

    val biometricEnabled: StateFlow<Boolean> = prefs
        .map { KEY_TOKEN_BIOMETRIC in it }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val _notice = MutableStateFlow<String?>(null)

    /** Why the user was signed out, when it was not their own doing. */
    val notice: StateFlow<String?> = _notice.asStateFlow()

    override suspend fun token(): String? = signedIn()?.token

    override suspend fun userId(): String? = signedIn()?.userId

    private suspend fun signedIn(): Session? {
        val loaded = session.first { it != SessionState.Loading }
        return (loaded as? SessionState.SignedIn)?.session
    }

    suspend fun save(session: Session) {
        dataStore.edit {
            it.clear()
            it[KEY_TOKEN] = cipher.encrypt(session.token)
            it[KEY_NAME] = cipher.encrypt(session.displayName)
            it[KEY_USER_ID] = cipher.encrypt(session.userId)
        }
        _notice.value = null
    }

    /** Logout, a 401 and a changed enrollment all end here; the biometric key goes too. */
    override suspend fun clear() {
        dataStore.edit { it.clear() }
        unlockedToken.value = null
        biometricCipher.deleteKey()
    }

    /** The cipher to hand to the "turn on" prompt, or null if this device cannot make the key. */
    fun biometricEnableCipher(): Cipher? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        return try {
            biometricCipher.encryptCipher()
        } catch (e: GeneralSecurityException) {
            null
        } catch (e: IllegalStateException) {
            null
        }
    }

    suspend fun enableBiometric(authenticated: Cipher) {
        val token = token() ?: return
        val sealed = biometricCipher.seal(authenticated, token)
        // Set before the edit: the session must not pass through Locked on the way.
        unlockedToken.value = token
        dataStore.edit {
            it[KEY_TOKEN_BIOMETRIC] = sealed
            it.remove(KEY_TOKEN)
        }
    }

    suspend fun disableBiometric() {
        val token = token() ?: return
        dataStore.edit {
            it[KEY_TOKEN] = cipher.encrypt(token)
            it.remove(KEY_TOKEN_BIOMETRIC)
        }
        unlockedToken.value = null
        biometricCipher.deleteKey()
    }

    /**
     * The cipher to hand to the unlock prompt. Null when there is nothing to unlock, or when a
     * new fingerprint or face was enrolled: that invalidates the key, so the session ends here
     * (queued edits stay for the next sign-in) and the login screen explains why.
     */
    suspend fun unlockCipher(): Cipher? {
        val sealed = prefs.first()[KEY_TOKEN_BIOMETRIC] ?: return null
        return try {
            biometricCipher.decryptCipher(sealed)
        } catch (e: KeyPermanentlyInvalidatedException) {
            clear()
            _notice.value = context.getString(R.string.biometrics_changed_notice)
            null
        }
    }

    /** Returns false if [authenticated] could not open the stored token. */
    suspend fun completeUnlock(authenticated: Cipher): Boolean {
        val sealed = prefs.first()[KEY_TOKEN_BIOMETRIC] ?: return false
        unlockedToken.value = biometricCipher.open(authenticated, sealed) ?: return false
        return true
    }

    private companion object {
        const val KEY_ALIAS = "unieats_session_key"
        const val BIOMETRIC_KEY_ALIAS = "unieats_session_biometric_key"
        val KEY_TOKEN = stringPreferencesKey("jwt")
        val KEY_TOKEN_BIOMETRIC = stringPreferencesKey("jwt_biometric")
        val KEY_NAME = stringPreferencesKey("display_name")
        val KEY_USER_ID = stringPreferencesKey("user_id")
    }
}
