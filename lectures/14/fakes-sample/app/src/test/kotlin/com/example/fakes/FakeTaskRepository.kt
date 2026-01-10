package com.example.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FAKE IMPLEMENTATION for testing.
 *
 * Why Fakes over Mocks?
 *
 * 1. SIMPLER: No complex mock setup syntax
 * 2. REUSABLE: Same fake works for multiple tests
 * 3. REALISTIC: Implements actual behavior
 * 4. MAINTAINABLE: Changes to interface update fake at compile time
 * 5. READABLE: Test code is cleaner and more focused
 *
 * When to use Fakes:
 * - Repository/data layer testing
 * - When behavior matters, not just return values
 * - For complex stateful interactions
 *
 * When to use Mocks:
 * - Verifying specific method calls
 * - One-off test scenarios
 * - External service interactions
 */
class FakeTaskRepository : TaskRepository {

  // Internal state that tests can inspect
  private val _tasks = MutableStateFlow<List<Task>>(emptyList())

  // Track method calls for verification
  val addedTasks = mutableListOf<Task>()
  val deletedTaskIds = mutableListOf<String>()
  val completedTaskIds = mutableListOf<String>()

  // Control behavior from tests
  var shouldFailOnAdd = false
  var addDelay: Long = 0

  override fun getTasks(): Flow<List<Task>> = _tasks.asStateFlow()

  override suspend fun getTask(id: String): Task? =
    _tasks.value.find { it.id == id }

  override suspend fun addTask(task: Task) {
    if (shouldFailOnAdd) {
      throw IllegalStateException("Simulated failure")
    }
    if (addDelay > 0) {
      kotlinx.coroutines.delay(addDelay)
    }
    addedTasks.add(task)
    _tasks.value = _tasks.value + task
  }

  override suspend fun updateTask(task: Task) {
    _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }
  }

  override suspend fun deleteTask(id: String) {
    deletedTaskIds.add(id)
    _tasks.value = _tasks.value.filter { it.id != id }
  }

  override suspend fun completeTask(id: String) {
    completedTaskIds.add(id)
    _tasks.value = _tasks.value.map {
      if (it.id == id) it.copy(isCompleted = true) else it
    }
  }

  // Helper methods for test setup
  fun setInitialTasks(tasks: List<Task>) {
    _tasks.value = tasks
    addedTasks.clear()
    deletedTaskIds.clear()
    completedTaskIds.clear()
  }

  fun reset() {
    _tasks.value = emptyList()
    addedTasks.clear()
    deletedTaskIds.clear()
    completedTaskIds.clear()
    shouldFailOnAdd = false
    addDelay = 0
  }
}
