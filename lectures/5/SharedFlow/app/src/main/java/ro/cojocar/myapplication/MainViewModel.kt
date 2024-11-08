package ro.cojocar.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private const val delayMilli = 2000L

class MainViewModel : ViewModel() {
  private val _sharedFlow = MutableSharedFlow<Int>()
  val sharedFlow: SharedFlow<Int> = _sharedFlow

  val regularFlow: Flow<Int> = flow {
    repeat(1_000_000) {
      emit(it)
      delay(delayMilli)
    }
  }

  init {
    viewModelScope.launch {
      repeat(1_000_000) {
        _sharedFlow.emit(it)
        delay(delayMilli)
      }
    }
  }
}
