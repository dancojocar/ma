package com.unieats.app.data.repository

import com.unieats.app.data.auth.Session
import com.unieats.app.data.auth.SessionState
import com.unieats.app.data.auth.TokenStore
import com.unieats.app.data.remote.ApiClient
import com.unieats.app.data.remote.apiCall
import com.unieats.app.data.sync.OutboxSync
import com.unieats.app.data.sync.SyncScheduler
import kotlinx.coroutines.flow.StateFlow
import javax.crypto.Cipher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val outboxSync: OutboxSync,
    private val syncScheduler: SyncScheduler
) {
    val session: StateFlow<SessionState> = tokenStore.session
    val biometricEnabled: StateFlow<Boolean> = tokenStore.biometricEnabled
    val signedOutNotice: StateFlow<String?> = tokenStore.notice

    suspend fun login(email: String, password: String): Result<Unit> = apiCall {
        val response = api.login(email.trim(), password)
        // Before the token is saved, so a replay can never send these with the new user's token.
        outboxSync.discardOtherUsers(response.user.id)
        tokenStore.save(Session(response.token, response.user.displayName, response.user.id))
        // Edits this user queued before the session ended can be replayed now.
        syncScheduler.requestSync()
    }

    /** Sign out on purpose: unlike a 401, queued edits are thrown away with the session. */
    suspend fun logout() {
        syncScheduler.cancel()
        outboxSync.discardAll()
        tokenStore.clear()
    }

    fun biometricEnableCipher(): Cipher? = tokenStore.biometricEnableCipher()

    suspend fun enableBiometric(authenticated: Cipher) = tokenStore.enableBiometric(authenticated)

    suspend fun disableBiometric() = tokenStore.disableBiometric()

    suspend fun unlockCipher(): Cipher? = tokenStore.unlockCipher()

    suspend fun completeUnlock(authenticated: Cipher) {
        // While locked, replays stopped with NeedsLogin; the token is readable again now.
        if (tokenStore.completeUnlock(authenticated)) syncScheduler.requestSync()
    }
}
