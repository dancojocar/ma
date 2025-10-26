package ro.cojocar.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private const val delayMilli = 1000L
private const val delayStartMilli = 4000L

class MainViewModel : ViewModel() {

  private val _sharedFlow = MutableSharedFlow<Int>(replay = 0)
  val sharedFlow: SharedFlow<Int> = _sharedFlow

  val times = 10
  val totalCollectors = 4

  val regularFlow: Flow<Int> = flow {
    repeat(times) {
      emit(it)
      delay(delayMilli)
    }
  }

  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState

  // Keep track of collector jobs so we can cancel them
  private val collectorJobs = mutableListOf<Job>()

  fun startCollecting() {
    // Cancel previous collectors
    collectorJobs.forEach { it.cancel() }
    collectorJobs.clear()

    // Reset UI state
    _uiState.value = UiState(isCollecting = true, messages = listOf("🔄 Starting collection..."))

    // Start collectors *before* starting shared emissions
    collectorJobs += collectRegular("Regular Flow 1") { copy(regular1 = it) }
    collectorJobs += collectRegular("Regular Flow 2", delayStart = true) { copy(regular2 = it) }
    collectorJobs += collectShared("Shared Flow 1") { copy(shared1 = it) }
    collectorJobs += collectShared("Shared Flow 2", delayStart = true) { copy(shared2 = it) }

    // Now start emitting the shared flow
    startSharedFlow()
  }

  private fun collectRegular(
    name: String,
    delayStart: Boolean = false,
    update: UiState.(Int) -> UiState
  ): Job {
    return viewModelScope.launch {
      if (delayStart) delay(delayStartMilli)
      regularFlow.collect { value ->
        _uiState.value = _uiState.value.update(value)
        if (value == times - 1) markCompleted(name)
      }
    }
  }

  private fun collectShared(
    name: String,
    delayStart: Boolean = false,
    update: UiState.(Int) -> UiState
  ): Job {
    return viewModelScope.launch {
      if (delayStart) delay(delayStartMilli)
      sharedFlow.collect { value ->
        _uiState.value = _uiState.value.update(value)
        if (value == times - 1) markCompleted(name)
      }
    }
  }

  // Start emitting shared flow values
  fun startSharedFlow() {
    viewModelScope.launch {
      repeat(times) {
        _sharedFlow.emit(it)
        delay(delayMilli)
      }
    }
  }

  private fun markCompleted(name: String) {
    val updated = _uiState.value.copy(
      completed = _uiState.value.completed + 1,
      messages = _uiState.value.messages + "✅ $name completed"
    )
    _uiState.value = updated

    if (updated.completed == totalCollectors) {
      _uiState.value = updated.copy(
        isCollecting = false,
        messages = updated.messages + "🎉 All flows completed!"
      )
    }
  }
}

data class UiState(
  val regular1: Int = 0,
  val regular2: Int = 0,
  val shared1: Int = 0,
  val shared2: Int = 0,
  val completed: Int = 0,
  val isCollecting: Boolean = false,
  val messages: List<String> = emptyList()
)
