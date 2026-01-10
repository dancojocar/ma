package com.example.turbine

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

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
          CounterScreen()
        }
      }
    }
  }
}

@Composable
fun CounterScreen(viewModel: CounterViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Turbine Flow Testing Demo",
      style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text(
      text = "Count: ${uiState.count}",
      style = MaterialTheme.typography.displayLarge
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { viewModel.decrement() }) {
        Text("-")
      }
      Button(onClick = { viewModel.reset() }) {
        Text("Reset")
      }
      Button(onClick = { viewModel.increment() }) {
        Text("+")
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(onClick = { viewModel.loadData() }) {
      Text("Load Data")
    }

    if (uiState.isLoading) {
      CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }

    if (uiState.message.isNotEmpty()) {
      Text(
        text = uiState.message,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(16.dp)
      )
    }

    uiState.error?.let { error ->
      Text(
        text = "Error: $error",
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp)
      )
    }
  }
}
