package com.unieats.app.ui.navigation

import android.content.Intent
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.BackStackMatchResult
import androidx.navigation3.runtime.deeplink.DeepLinkMatcher
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.invoke
import androidx.navigation3.runtime.deeplink.withBackStack
import kotlinx.serialization.serializer

const val DEEP_LINK_APP = "unieats://spots"
const val DEEP_LINK_WEB = "https://unieats.app/spots"

/** Both patterns resolve to <basePath>/{spotId}, e.g. unieats://spots/spot-3. */
private val spotDetailMatchers: List<DeepLinkMatcher<*, *>> = listOf(DEEP_LINK_APP, DEEP_LINK_WEB).map { base ->
    UriDeepLinkMatcher(DeepLinkUri("$base/{spotId}"), serializer<SpotDetail>())
        .withBackStack { match -> listOf(SpotList, match.key) }
}

/** The back stack to start with: the list, or list + detail when the intent is a spot deep link. */
fun backStackFor(intent: Intent?): List<NavKey> {
    if (intent?.data == null) return listOf(SpotList)
    val request = DeepLinkRequest(intent)
    val best = spotDetailMatchers.mapNotNull { it.match(request) }.maxOrNull()
    return when (best) {
        is BackStackMatchResult<*, *> -> @Suppress("UNCHECKED_CAST") (best.backStack as List<NavKey>)
        null -> listOf(SpotList)
        else -> listOf(SpotList, best.key as NavKey)
    }
}
