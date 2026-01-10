package com.example.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Data model for a Task item.
 */
data class Task(
  val id: String,
  val title: String,
  val description: String = "",
  val isCompleted: Boolean = false
)

/**
 * Repository interface for Task operations.
 *
 * This interface enables the Fakes pattern:
 * - Production code uses a real implementation (e.g., database, network)
 * - Tests use a Fake implementation with controlled behavior
 */
interface TaskRepository {
  fun getTasks(): Flow<List<Task>>
  suspend fun getTask(id: String): Task?
  suspend fun addTask(task: Task)
  suspend fun updateTask(task: Task)
  suspend fun deleteTask(id: String)
  suspend fun completeTask(id: String)
}

/**
 * Production implementation that would use a real data source.
 * (Simplified for demo purposes)
 */
class TaskRepositoryImpl : TaskRepository {
  private val tasks = MutableStateFlow<List<Task>>(emptyList())

  override fun getTasks(): Flow<List<Task>> = tasks.asStateFlow()

  override suspend fun getTask(id: String): Task? =
    tasks.value.find { it.id == id }

  override suspend fun addTask(task: Task) {
    tasks.value += task
  }

  override suspend fun updateTask(task: Task) {
    tasks.value = tasks.value.map { if (it.id == task.id) task else it }
  }

  override suspend fun deleteTask(id: String) {
    tasks.value = tasks.value.filter { it.id != id }
  }

  override suspend fun completeTask(id: String) {
    tasks.value = tasks.value.map {
      if (it.id == id) it.copy(isCompleted = true) else it
    }
  }
}
