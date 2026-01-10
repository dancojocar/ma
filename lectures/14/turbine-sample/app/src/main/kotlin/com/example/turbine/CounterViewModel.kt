package com.example.turbine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel with StateFlow for UI state management.
 * Provides a clean example for testing with Turbine.
 */
class CounterViewModel(
  private val repository: CounterRepository = CounterRepository()
) : ViewModel() {

  private val _uiState = MutableStateFlow(CounterUiState())
  val uiState: StateFlow<CounterUiState> = _uiState.asStateFlow()

  init {
    // Observe repository state
    viewModelScope.launch {
      repository.counterState.collect { count ->
        _uiState.value = _uiState.value.copy(count = count)
      }
    }
  }

  fun increment() {
    repository.increment()
  }

  fun decrement() {
    repository.decrement()
  }

  fun reset() {
    repository.reset()
  }

  fun loadData() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, error = null)
      try {
        repository.fetchDataWithPossibleError(shouldFail = false)
          .collect { message ->
            _uiState.value = _uiState.value.copy(
              message = message,
              isLoading = message == "Loading..."
            )
          }
      } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          error = e.message
        )
      }
    }
  }
}

data class CounterUiState(
  val count: Int = 0,
  val message: String = "",
  val isLoading: Boolean = false,
  val error: String? = null
)
