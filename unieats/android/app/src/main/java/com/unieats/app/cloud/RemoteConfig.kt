package com.unieats.app.cloud

import com.unieats.shared.RemoteFlags
import com.unieats.app.data.remote.ApiClient
import com.unieats.app.data.remote.apiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Feature flags from GET /api/config, the same shape as Firebase Remote Config: in-app defaults
 * until the first successful fetch, then the server's values. If the fetch fails the current
 * values stay active.
 */
@Singleton
class RemoteConfig @Inject constructor(private val api: ApiClient) : FeatureFlags {

    private val _flags = MutableStateFlow(RemoteFlags())
    override val flags: StateFlow<RemoteFlags> = _flags.asStateFlow()

    suspend fun fetchAndActivate(): Result<RemoteFlags> = apiCall {
        api.getConfig().flags.also { _flags.value = it }
    }
}
