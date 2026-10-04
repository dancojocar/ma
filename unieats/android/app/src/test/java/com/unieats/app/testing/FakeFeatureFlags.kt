package com.unieats.app.testing

import com.unieats.app.cloud.FeatureFlags
import com.unieats.shared.RemoteFlags
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFeatureFlags : FeatureFlags {
    override val flags = MutableStateFlow(RemoteFlags())
}
