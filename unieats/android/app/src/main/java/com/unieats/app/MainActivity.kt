package com.unieats.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavKey
import com.unieats.app.ui.navigation.UniEatsNavDisplay
import com.unieats.app.ui.navigation.backStackFor
import com.unieats.app.ui.theme.UniEatsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Set by a deep link that arrives while the app is running (launchMode singleTop); consumed once.
    private var pendingDeepLink: List<NavKey>? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val startKeys = backStackFor(intent)
        setContent {
            UniEatsTheme {
                UniEatsNavDisplay(
                    startKeys = startKeys,
                    pendingDeepLink = pendingDeepLink,
                    onDeepLinkConsumed = { pendingDeepLink = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.data != null) pendingDeepLink = backStackFor(intent)
    }
}
