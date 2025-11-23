package com.example.android.repeatingalarm.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.android.repeatingalarm.R

@Composable
fun MainScreen(
  onOneTimeAlarmClick: () -> Unit,
  onRepeatingAlarmClick: () -> Unit,
  onCancelAlarmClick: () -> Unit,
  onOpenSettingsClick: () -> Unit
) {
  Column(modifier = Modifier
    .fillMaxSize()
    .statusBarsPadding()
  ) {
    Text(text = stringResource(id = R.string.intro_message))
    Button(onClick = onOneTimeAlarmClick) {
      Text(text = "Set One-Time Alarm")
    }
    Button(onClick = onRepeatingAlarmClick) {
      Text(text = "Set Repeating Alarm")
    }
    Button(onClick = onCancelAlarmClick) {
      Text(text = "Cancel Alarm")
    }
    Button(onClick = onOpenSettingsClick) {
      Text(text = "Open App Settings")
    }
  }
}
