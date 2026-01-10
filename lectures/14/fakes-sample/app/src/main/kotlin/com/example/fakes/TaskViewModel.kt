package com.example.fakes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Task list screen.
 * Uses constructor injection for easy testing with Fakes.
 */
class TaskViewModel(
  private val repository: TaskRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(TaskUiState())
  val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

  init {
    observeTasks()
  }

  private fun observeTasks() {
    viewModelScope.launch {
      repository.getTasks().collect { tasks ->
        _uiState.value = _uiState.value.copy(
          tasks = tasks,
          completedCount = tasks.count { it.isCompleted },
          pendingCount = tasks.count { !it.isCompleted }
        )
      }
    }
  }

  fun addTask(title: String, description: String = "") {
    if (title.isBlank()) {
      _uiState.value = _uiState.value.copy(error = "Title cannot be empty")
      return
    }

    viewModelScope.launch {
      val task = Task(
        id = generateId(),
        title = title,
        description = description
      )
      repository.addTask(task)
      _uiState.value = _uiState.value.copy(error = null)
    }
  }

  fun completeTask(id: String) {
    viewModelScope.launch {
      repository.completeTask(id)
    }
  }

  fun deleteTask(id: String) {
    viewModelScope.launch {
      repository.deleteTask(id)
    }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  private fun generateId(): String =
    java.util.UUID.randomUUID().toString().take(8)
}

data class TaskUiState(
  val tasks: List<Task> = emptyList(),
  val completedCount: Int = 0,
  val pendingCount: Int = 0,
  val error: String? = null
)
