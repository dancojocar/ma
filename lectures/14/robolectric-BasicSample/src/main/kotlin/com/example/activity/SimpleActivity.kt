package com.example.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Simple Activity demonstrating:
 * - Jetpack Compose UI
 * - Edge-to-edge display with camera cutout support
 * - Testable composables with semantic test tags
 * - Integration with Robolectric for local testing
 */
class SimpleActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MaterialTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
          color = MaterialTheme.colorScheme.background
        ) {
          SimpleScreen()
        }
      }
    }
  }
}

/**
 * A simple screen composable that can be tested with Robolectric.
 */
@Composable
fun SimpleScreen() {
  var count by remember { mutableIntStateOf(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Hello, Kotlin!",
      style = MaterialTheme.typography.headlineLarge,
      modifier = Modifier.testTag("greeting")
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = "Count: $count",
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.testTag("counter")
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { count++ },
        modifier = Modifier.testTag("incrementButton")
      ) {
        Text("Increment")
      }

      Button(
        onClick = { count-- },
        modifier = Modifier.testTag("decrementButton")
      ) {
        Text("Decrement")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = { count = 0 },
      modifier = Modifier.testTag("resetButton"),
      colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary
      )
    ) {
      Text("Reset")
    }
  }
}
