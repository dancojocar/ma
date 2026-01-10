package com.example.screenshot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
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
          DemoScreen()
        }
      }
    }
  }
}

@Composable
fun DemoScreen() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Paparazzi Screenshot Testing",
      style = MaterialTheme.typography.headlineMedium
    )

    ProfileCard(
      name = "Jane Doe",
      email = "jane@example.com"
    )

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      StatCard(
        title = "TASKS",
        value = "12",
        subtitle = "completed",
        color = Color(0xFF4CAF50),
        modifier = Modifier.weight(1f)
      )
      StatCard(
        title = "PENDING",
        value = "5",
        subtitle = "in progress",
        color = Color(0xFFFF9800),
        modifier = Modifier.weight(1f)
      )
    }

    ActionButton(
      text = "Get Started",
      onClick = { }
    )

    ActionButton(
      text = "Loading...",
      onClick = { },
      isLoading = true
    )
  }
}
