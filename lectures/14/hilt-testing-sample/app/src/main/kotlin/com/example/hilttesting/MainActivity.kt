package com.example.hilttesting

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
          GreetingScreen()
        }
      }
    }
  }
}

@Composable
fun GreetingScreen(viewModel: GreetingViewModel = hiltViewModel()) {
  val greeting by viewModel.greeting.collectAsStateWithLifecycle()
  var name by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Hilt Testing Demo",
      style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(24.dp))

    if (greeting.isNotEmpty()) {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("greetingCard")
      ) {
        Text(
          text = greeting,
          style = MaterialTheme.typography.titleLarge,
          modifier = Modifier
            .padding(16.dp)
            .testTag("greetingText")
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Your name") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("nameInput")
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(
        onClick = { viewModel.loadGreeting() },
        modifier = Modifier.testTag("loadGreetingButton")
      ) {
        Text("Default Greeting")
      }

      Button(
        onClick = { viewModel.loadPersonalizedGreeting(name) },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("personalizedButton")
      ) {
        Text("Personalized")
      }
    }
  }
}
