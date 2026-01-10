package com.example.fakes

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Tests demonstrating the FAKE pattern.
 *
 * Notice how clean these tests are:
 * - No mock() calls or complex setup
 * - Fake provides realistic behavior
 * - Tests are readable and focused on behavior
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  // Our fake - reused across all tests
  private lateinit var fakeRepository: FakeTaskRepository
  private lateinit var viewModel: TaskViewModel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    fakeRepository = FakeTaskRepository()
    viewModel = TaskViewModel(fakeRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ============================================
  // Basic CRUD Tests with Fakes
  // ============================================

  @Test
  fun `adding task updates the task list`() = runTest {
    viewModel.uiState.test {
      // Initial state
      val initial = awaitItem()
      assertThat(initial.tasks).isEmpty()

      // Add a task
      viewModel.addTask("Test Task", "Description")
      advanceUntilIdle()

      // Verify task was added
      val afterAdd = awaitItem()
      assertThat(afterAdd.tasks).hasSize(1)
      assertThat(afterAdd.tasks[0].title).isEqualTo("Test Task")
      assertThat(afterAdd.pendingCount).isEqualTo(1)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `completing task updates completed count`() = runTest {
    // Setup: Add initial task using the fake
    val task = Task(id = "1", title = "Complete me")
    fakeRepository.setInitialTasks(listOf(task))

    // Recreate ViewModel to pick up initial state
    viewModel = TaskViewModel(fakeRepository)
    advanceUntilIdle()

    viewModel.uiState.test {
      val initial = awaitItem()
      assertThat(initial.completedCount).isEqualTo(0)
      assertThat(initial.pendingCount).isEqualTo(1)

      // Complete the task
      viewModel.completeTask("1")
      advanceUntilIdle()

      val afterComplete = awaitItem()
      assertThat(afterComplete.completedCount).isEqualTo(1)
      assertThat(afterComplete.pendingCount).isEqualTo(0)

      // Verify fake tracked the call
      assertThat(fakeRepository.completedTaskIds).contains("1")

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `deleting task removes it from list`() = runTest {
    // Setup with fake
    fakeRepository.setInitialTasks(
      listOf(
        Task(id = "1", title = "Task 1"),
        Task(id = "2", title = "Task 2")
      )
    )
    viewModel = TaskViewModel(fakeRepository)
    advanceUntilIdle()

    viewModel.uiState.test {
      val initial = awaitItem()
      assertThat(initial.tasks).hasSize(2)

      // Delete a task
      viewModel.deleteTask("1")
      advanceUntilIdle()

      val afterDelete = awaitItem()
      assertThat(afterDelete.tasks).hasSize(1)
      assertThat(afterDelete.tasks[0].id).isEqualTo("2")

      // Verify fake tracked the call
      assertThat(fakeRepository.deletedTaskIds).contains("1")

      cancelAndIgnoreRemainingEvents()
    }
  }

  // ============================================
  // Error Handling Tests
  // ============================================

  @Test
  fun `adding task with blank title shows error`() = runTest {
    viewModel.uiState.test {
      awaitItem() // Initial

      viewModel.addTask("", "Some description")
      advanceUntilIdle()

      val afterError = awaitItem()
      assertThat(afterError.error).isEqualTo("Title cannot be empty")
      assertThat(afterError.tasks).isEmpty()

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `clearError removes error message`() = runTest {
    viewModel.uiState.test {
      awaitItem() // Initial

      // Trigger error
      viewModel.addTask("", "")
      advanceUntilIdle()

      val withError = awaitItem()
      assertThat(withError.error).isNotNull()

      // Clear error
      viewModel.clearError()

      val cleared = awaitItem()
      assertThat(cleared.error).isNull()

      cancelAndIgnoreRemainingEvents()
    }
  }

  // ============================================
  // Test using Fake's control features
  // ============================================

  @Test
  fun `fake tracks all added tasks`() = runTest {
    viewModel.addTask("Task 1", "Desc 1")
    viewModel.addTask("Task 2", "Desc 2")
    viewModel.addTask("Task 3", "Desc 3")
    advanceUntilIdle()

    // Fake keeps track of what was added
    assertThat(fakeRepository.addedTasks).hasSize(3)
    assertThat(fakeRepository.addedTasks.map { it.title })
      .containsExactly("Task 1", "Task 2", "Task 3")
  }

  @Test
  fun `multiple operations work correctly together`() = runTest {
    // Add tasks
    viewModel.addTask("Task 1")
    viewModel.addTask("Task 2")
    viewModel.addTask("Task 3")
    advanceUntilIdle()

    // Get the task IDs from the fake
    val taskIds = fakeRepository.addedTasks.map { it.id }

    // Complete first, delete second
    viewModel.completeTask(taskIds[0])
    viewModel.deleteTask(taskIds[1])
    advanceUntilIdle()

    viewModel.uiState.test {
      val state = awaitItem()
      assertThat(state.tasks).hasSize(2)
      assertThat(state.completedCount).isEqualTo(1)
      assertThat(state.pendingCount).isEqualTo(1)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
