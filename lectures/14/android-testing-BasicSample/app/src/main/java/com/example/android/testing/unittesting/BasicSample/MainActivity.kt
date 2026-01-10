package com.example.android.testing.unittesting.BasicSample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/**
 * Main Activity demonstrating:
 * - Jetpack Compose UI
 * - Edge-to-edge display with camera cutout support
 * - Integration with testable business logic (EmailValidator)
 */
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Enable edge-to-edge display for modern Android UI
    enableEdgeToEdge()

    setContent {
      MaterialTheme {
        Surface(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
          color = MaterialTheme.colorScheme.background
        ) {
          EmailValidationScreen()
        }
      }
    }
  }
}

/**
 * Email validation screen composable.
 * Demonstrates real-time validation using the EmailValidator.
 */
@Composable
fun EmailValidationScreen() {
  var email by remember { mutableStateOf(TextFieldValue("")) }
  var validationResult by remember { mutableStateOf<Boolean?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Email Validator",
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.padding(bottom = 24.dp)
    )

    OutlinedTextField(
      value = email,
      onValueChange = {
        email = it
        validationResult = null  // Reset validation on change
      },
      label = { Text("Enter email address") },
      modifier = Modifier
        .fillMaxWidth()
        .semantics { testTag = "emailInput" },
      singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
      onClick = {
        validationResult = EmailValidator.isValidEmail(email.text)
      },
      modifier = Modifier.semantics { testTag = "validateButton" }
    ) {
      Text("Validate")
    }

    Spacer(modifier = Modifier.height(24.dp))

    validationResult?.let { isValid ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .semantics { testTag = "resultCard" },
        colors = CardDefaults.cardColors(
          containerColor = if (isValid) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
      ) {
        Text(
          text = if (isValid) "✓ Valid email address" else "✗ Invalid email address",
          modifier = Modifier
            .padding(16.dp)
            .semantics { testTag = "resultText" },
          color = Color.White,
          style = MaterialTheme.typography.bodyLarge
        )
      }
    }
  }
}
