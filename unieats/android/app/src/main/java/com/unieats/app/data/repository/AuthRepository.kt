package com.unieats.app.data.repository

import com.unieats.app.data.auth.Session
import com.unieats.app.data.auth.TokenStore
import com.unieats.app.data.remote.ApiClient
import com.unieats.app.data.remote.apiCall
import com.unieats.app.data.sync.SyncScheduler
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: ApiClient,
    private val tokenStore: TokenStore,
    private val syncScheduler: SyncScheduler
) {
    val session: StateFlow<Session?> = tokenStore.session

    suspend fun login(email: String, password: String): Result<Unit> = apiCall {
        val response = api.login(email.trim(), password)
        tokenStore.save(Session(response.token, response.user.displayName))
        // Edits queued while signed out can be replayed now.
        syncScheduler.requestSync()
    }

    fun logout() = tokenStore.clear()
}
