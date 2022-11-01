package ro.cojocar.dan.coroutinedemo

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class MyViewModel : ViewModel() {
  private val _result = MutableLiveData<String>()
  val result: LiveData<String> = _result

  init {
    viewModelScope.launch {
      val computationalResult = doComputation()
      _result.value = computationalResult
    }
  }
}


class MyNewViewModel : ViewModel() {
  val result = liveData {
    emit(doComputation())
  }
}


private suspend fun doComputation(): String {
  TODO("not implemented")
}

class Test {
  fun test() {
    val myViewModel = MyViewModel()
    myViewModel.result
    val myNewViewModel = MyNewViewModel()
    myNewViewModel.result
  }
}