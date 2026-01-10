package com.example.dan.crashlyticsdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dan.crashlyticsdemo.ui.theme.RemoteConfigDemoTheme
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class MainActivity : ComponentActivity() {

  private lateinit var remoteConfig: FirebaseRemoteConfig
  private val helloMessage = mutableStateOf("Hello World!")


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    FirebaseCrashlytics.getInstance().log("app started")

    remoteConfig = Firebase.remoteConfig
    val configSettings = remoteConfigSettings {
      minimumFetchIntervalInSeconds = 60
    }
    remoteConfig.setConfigSettingsAsync(configSettings)
    remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

    fetchValues()

    setContent {
      RemoteConfigDemoTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainScreen(
            helloMessage = helloMessage.value,
            onCrashClick = { onCrashClick() }
          )
        }
      }
    }
  }

  private fun onCrashClick() {
    if (remoteConfig.getString(HIDE_CRASH_LOGIC)
      == remoteConfig.getString(HIDE_CRASH_LOGIC_VALUE)
    ) {
      throw RuntimeException("Oppps!")
    } else {
      Toast.makeText(
        this,
        remoteConfig.getString(HIDE_CRASH_NO_LOGIC),
        Toast.LENGTH_LONG
      ).show()
    }
  }

  private fun fetchValues() {
    Log.d(TAG, "Fetching config params")
    helloMessage.value = remoteConfig.getString(HELLO_MESSAGE)

    remoteConfig.fetchAndActivate()
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          val updated = task.result
          Log.d(TAG, "Config params updated: $updated")
          Toast.makeText(
            this, "Fetch and activate succeeded",
            Toast.LENGTH_SHORT
          ).show()
        } else {
          Log.d(TAG, "Failed to fetch", task.exception)
          Toast.makeText(
            this, "Fetch failed",
            Toast.LENGTH_SHORT
          ).show()
        }
        displayWelcomeMessage()
      }
  }

  private fun displayWelcomeMessage() {
    helloMessage.value = remoteConfig.getString(HELLO_MESSAGE)
  }

  companion object {
    private const val TAG = "RemoteConfigDemo"
    private const val HIDE_CRASH_LOGIC = "hide_crash_logic"
    private const val HIDE_CRASH_LOGIC_VALUE = "hide_crash_value"
    private const val HIDE_CRASH_NO_LOGIC = "hide_crash_nologic"
    private const val HELLO_MESSAGE = "hello_message"
  }
}

@Composable
fun MainScreen(
  helloMessage: String,
  onCrashClick: () -> Unit
) {
  Scaffold { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = helloMessage,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(bottom = 24.dp)
      )
      Button(onClick = onCrashClick) {
        Text(text = "Crash")
      }
    }
  }
}
