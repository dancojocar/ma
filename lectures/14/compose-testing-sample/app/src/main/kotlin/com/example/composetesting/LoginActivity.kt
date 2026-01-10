package com.example.composetesting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

class LoginActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MaterialTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          LoginScreen()
        }
      }
    }
  }
}

@Composable
fun LoginScreen() {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var error by remember { mutableStateOf("") }
  var isLoggedIn by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "Welcome Back",
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.testTag("title")
    )

    Spacer(modifier = Modifier.height(32.dp))

    OutlinedTextField(
      value = email,
      onValueChange = {
        email = it
        error = ""
      },
      label = { Text("Email") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("emailInput")
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
      value = password,
      onValueChange = {
        password = it
        error = ""
      },
      label = { Text("Password") },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("passwordInput")
    )

    if (error.isNotEmpty()) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.testTag("errorMessage")
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = {
        if (!email.contains("@")) {
          error = "Invalid email"
        } else if (password.length < 6) {
          error = "Password too short"
        } else {
          isLoggedIn = true
        }
      },
      modifier = Modifier
        .fillMaxWidth()
        .testTag("loginButton")
    ) {
      Text("Login")
    }

    if (isLoggedIn) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = "Success!",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.testTag("successMessage")
      )
    }
  }
}
