package com.example.fakes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
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
          // In production, you'd use Hilt or a factory
          val viewModel: TaskViewModel = viewModel {
            TaskViewModel(TaskRepositoryImpl())
          }
          TaskScreen(viewModel)
        }
      }
    }
  }
}

@Composable
fun TaskScreen(viewModel: TaskViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(onClick = { showAddDialog = true }) {
        Icon(Icons.Default.Add, contentDescription = "Add Task")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp)
    ) {
      Text(
        text = "Fakes Pattern Demo",
        style = MaterialTheme.typography.headlineMedium
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Stats card
      Card(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${uiState.pendingCount}", style = MaterialTheme.typography.headlineLarge)
            Text("Pending")
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${uiState.completedCount}", style = MaterialTheme.typography.headlineLarge)
            Text("Completed")
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (uiState.tasks.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Text("No tasks yet. Add one!")
        }
      } else {
        LazyColumn {
          items(uiState.tasks, key = { it.id }) { task ->
            TaskItem(
              task = task,
              onComplete = { viewModel.completeTask(task.id) },
              onDelete = { viewModel.deleteTask(task.id) }
            )
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AddTaskDialog(
      onDismiss = { showAddDialog = false },
      onAdd = { title, desc ->
        viewModel.addTask(title, desc)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun TaskItem(task: Task, onComplete: () -> Unit, onDelete: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.titleMedium,
          textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
        )
        if (task.description.isNotEmpty()) {
          Text(
            text = task.description,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
      if (!task.isCompleted) {
        IconButton(onClick = onComplete) {
          Icon(Icons.Default.Check, contentDescription = "Complete")
        }
      }
      IconButton(onClick = onDelete) {
        Icon(Icons.Default.Delete, contentDescription = "Delete")
      }
    }
  }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Task") },
    text = {
      Column {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Title") },
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description (optional)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onAdd(title, description) },
        enabled = title.isNotBlank()
      ) {
        Text("Add")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
