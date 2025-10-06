package com.example.lifecycleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lifecycleapp.ui.theme.LifecycleAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LifecycleAppTheme {
        // A surface container using the 'background' color from the theme
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          Greeting("Android")
        }
      }
    }
  }

  override fun onStart() {
    super.onStart()
    logd("onStart was reached!")
  }

  override fun onResume() {
    super.onResume()
    logd("onResume was reached!")
  }

  override fun onPause() {
    super.onPause()
    logd("onPause was reached!")
  }

  override fun onRestart() {
    super.onRestart()
    logd("onRestart was reached!")
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Column(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text(
      text = "Hello $name!",
      modifier = modifier
    )
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  LifecycleAppTheme {
    Greeting("Android")
  }
}