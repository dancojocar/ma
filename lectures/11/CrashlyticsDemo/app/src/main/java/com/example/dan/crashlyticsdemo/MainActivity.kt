package com.example.dan.crashlyticsdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val crashlytics = FirebaseCrashlytics.getInstance()
    crashlytics.log("app started")
    setContent {
      CrashTestScreen()
    }
  }
}

@Composable
fun CrashTestScreen() {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Hello World!")
    Button(
      onClick = {
        throw RuntimeException("Testing a crash!")
      }) {
      Text(text = "Crash")
    }
  }
}
