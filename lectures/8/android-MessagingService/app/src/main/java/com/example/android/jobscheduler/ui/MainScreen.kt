package com.example.android.jobscheduler.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.android.jobscheduler.R
import com.example.android.jobscheduler.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
  val jobStatus by viewModel.jobStatus.observeAsState()
  val toast by viewModel.toast.observeAsState()

  var delay by remember { mutableStateOf("0") }
  var deadline by remember { mutableStateOf("0") }
  var workDuration by remember { mutableStateOf("1") }
  var requiresCharging by remember { mutableStateOf(false) }
  var requiresIdle by remember { mutableStateOf(false) }

  val context = LocalContext.current
  toast?.let {
    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
    viewModel.clearToast()
  }

  val statusColor = when (jobStatus?.action) {
    "started" -> colorResource(id = R.color.start_received)
    "stopped" -> colorResource(id = R.color.stop_received)
    else -> colorResource(id = R.color.none_received)
  }

  val statusTextColor = when (jobStatus?.action) {
    "started" -> Color.Black
    "stopped" -> Color.White
    else -> Color.White
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .padding(16.dp)
      .verticalScroll(rememberScrollState())
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 8.dp)
        .background(statusColor)
        .padding(8.dp)
    ) {
      Text(
        text = "Job Status: ${jobStatus?.jobId ?: "N/A"} - ${jobStatus?.action ?: "N/A"}",
        color = statusTextColor,
        style = MaterialTheme.typography.titleLarge
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    TextField(
      value = delay,
      onValueChange = { delay = it },
      label = { Text("Delay (s)", style = MaterialTheme.typography.titleMedium) }
    )
    TextField(
      value = deadline,
      onValueChange = { deadline = it },
      label = { Text("Deadline (s)", style = MaterialTheme.typography.titleMedium) }
    )
    TextField(
      value = workDuration,
      onValueChange = { workDuration = it },
      label = { Text("Work Duration (s)", style = MaterialTheme.typography.titleMedium) }
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
        checked = requiresCharging,
        onCheckedChange = { requiresCharging = it }
      )
      Text(text = "Requires Charging", style = MaterialTheme.typography.titleMedium)
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
        checked = requiresIdle,
        onCheckedChange = { requiresIdle = it }
      )
      Text(text = "Requires Idle", style = MaterialTheme.typography.titleMedium)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(onClick = {
      viewModel.scheduleJob(delay, deadline, requiresCharging, requiresIdle, workDuration)
    }) {
      Text(text = "Schedule Job")
    }

    Button(onClick = { viewModel.cancelAllJobs() }) {
      Text(text = "Cancel All Jobs")
    }

    Button(onClick = { viewModel.finishJob() }) {
      Text(text = "Finish Last Job")
    }
  }
}
