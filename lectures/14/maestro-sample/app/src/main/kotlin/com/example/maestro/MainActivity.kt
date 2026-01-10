package com.example.maestro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MaterialTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          TaskScreen()
        }
      }
    }
  }
}

@Composable
fun TaskScreen() {
  var taskText by remember { mutableStateOf("") }
  var tasks by remember { mutableStateOf(listOf<String>()) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .padding(top = 48.dp) // Status bar padding
  ) {
    Text(
      text = "Task Manager",
      style = MaterialTheme.typography.headlineMedium
    )

    Spacer(modifier = Modifier.height(16.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = taskText,
        onValueChange = { taskText = it },
        label = { Text("Enter task") },
        modifier = Modifier.weight(1f)
      )

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = {
          if (taskText.isNotBlank()) {
            tasks = tasks + taskText
            taskText = ""
          }
        }
      ) {
        Text("Add Task")
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    LazyColumn(
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(tasks) { task ->
        Card(
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = task,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
          )
        }
      }
    }
  }
}
