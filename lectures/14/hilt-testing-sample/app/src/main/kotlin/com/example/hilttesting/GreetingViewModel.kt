package com.example.hilttesting

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GreetingViewModel @Inject constructor(
  private val repository: GreetingRepository
) : ViewModel() {

  private val _greeting = MutableStateFlow("")
  val greeting: StateFlow<String> = _greeting.asStateFlow()

  fun loadGreeting() {
    _greeting.value = repository.getGreeting()
  }

  fun loadPersonalizedGreeting(name: String) {
    _greeting.value = repository.getPersonalizedGreeting(name)
  }
}
