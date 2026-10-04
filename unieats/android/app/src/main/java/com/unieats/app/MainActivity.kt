package com.unieats.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.unieats.app.ui.navigation.UniEatsNavGraph
import com.unieats.app.ui.theme.UniEatsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UniEatsTheme {
                // testTagsAsResourceId lets Maestro and UI Automator find composables by testTag.
                UniEatsNavGraph(modifier = Modifier.semantics { testTagsAsResourceId = true })
            }
        }
    }
}
