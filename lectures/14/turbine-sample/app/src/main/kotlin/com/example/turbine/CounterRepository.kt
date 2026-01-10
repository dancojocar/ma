package com.example.turbine

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

/**
 * Repository that provides data as Flows.
 * This demonstrates different Flow patterns that can be tested with Turbine.
 */
class CounterRepository {

  private val _counterState = MutableStateFlow(0)
  val counterState: StateFlow<Int> = _counterState.asStateFlow()

  fun increment() {
    _counterState.value++
  }

  fun decrement() {
    _counterState.value--
  }

  fun reset() {
    _counterState.value = 0
  }

  /**
   * A cold Flow that emits a countdown sequence.
   * Demonstrates testing cold flows with Turbine.
   */
  fun countdown(from: Int): Flow<Int> = flow {
    for (i in from downTo 0) {
      emit(i)
      if (i > 0) delay(100) // Small delay between emissions
    }
  }

  /**
   * A Flow that may emit errors.
   * Demonstrates testing error handling with Turbine.
   */
  fun fetchDataWithPossibleError(shouldFail: Boolean): Flow<String> = flow {
    emit("Loading...")
    delay(50)
    if (shouldFail) {
      throw IllegalStateException("Network error!")
    }
    emit("Success!")
  }
}
