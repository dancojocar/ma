package com.example.turbine

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Demonstrates Turbine for testing Kotlin Flows.
 *
 * Turbine provides a simple, powerful API for testing Flow emissions:
 * - awaitItem(): Wait for and return the next emission
 * - awaitError(): Wait for an error
 * - awaitComplete(): Wait for completion
 * - expectNoEvents(): Assert no events are pending
 * - cancelAndIgnoreRemainingEvents(): Cancel collection
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CounterRepositoryTest {

  private val repository = CounterRepository()

  // ============================================
  // Testing StateFlow with Turbine
  // ============================================

  @Test
  fun `counterState emits initial value of 0`() = runTest {
    repository.counterState.test {
      // First emission is the initial value
      assertThat(awaitItem()).isEqualTo(0)

      // No more emissions expected without action
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `increment increases counter value`() = runTest {
    repository.counterState.test {
      // Initial value
      assertThat(awaitItem()).isEqualTo(0)

      // Perform action
      repository.increment()

      // Verify new emission
      assertThat(awaitItem()).isEqualTo(1)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `multiple increments emit sequential values`() = runTest {
    repository.counterState.test {
      assertThat(awaitItem()).isEqualTo(0)

      repository.increment()
      assertThat(awaitItem()).isEqualTo(1)

      repository.increment()
      assertThat(awaitItem()).isEqualTo(2)

      repository.increment()
      assertThat(awaitItem()).isEqualTo(3)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `reset returns counter to 0`() = runTest {
    repository.counterState.test {
      assertThat(awaitItem()).isEqualTo(0)

      // Increment first
      repository.increment()
      repository.increment()
      assertThat(awaitItem()).isEqualTo(1)
      assertThat(awaitItem()).isEqualTo(2)

      // Reset
      repository.reset()
      assertThat(awaitItem()).isEqualTo(0)

      cancelAndIgnoreRemainingEvents()
    }
  }

  // ============================================
  // Testing Cold Flows with Turbine
  // ============================================

  @Test
  fun `countdown emits values from start to 0`() = runTest {
    repository.countdown(from = 3).test {
      assertThat(awaitItem()).isEqualTo(3)
      assertThat(awaitItem()).isEqualTo(2)
      assertThat(awaitItem()).isEqualTo(1)
      assertThat(awaitItem()).isEqualTo(0)

      // Flow completes after countdown
      awaitComplete()
    }
  }

  @Test
  fun `countdown from 0 emits only 0`() = runTest {
    repository.countdown(from = 0).test {
      assertThat(awaitItem()).isEqualTo(0)
      awaitComplete()
    }
  }

  // ============================================
  // Testing Error Handling with Turbine
  // ============================================

  @Test
  fun `fetchData success path emits Loading then Success`() = runTest {
    repository.fetchDataWithPossibleError(shouldFail = false).test {
      assertThat(awaitItem()).isEqualTo("Loading...")
      assertThat(awaitItem()).isEqualTo("Success!")
      awaitComplete()
    }
  }

  @Test
  fun `fetchData failure path emits Loading then throws error`() = runTest {
    repository.fetchDataWithPossibleError(shouldFail = true).test {
      assertThat(awaitItem()).isEqualTo("Loading...")

      // Expect an error
      val error = awaitError()
      assertThat(error).isInstanceOf(IllegalStateException::class.java)
      assertThat(error.message).isEqualTo("Network error!")
    }
  }
}
