package com.unieats.app.cloud

import com.unieats.shared.RemoteFlags
import kotlinx.coroutines.flow.StateFlow

/** Read side of remote config, what screens depend on. */
interface FeatureFlags {
    val flags: StateFlow<RemoteFlags>
}
