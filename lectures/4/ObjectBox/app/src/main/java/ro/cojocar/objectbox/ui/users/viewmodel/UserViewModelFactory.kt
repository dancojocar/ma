package ro.cojocar.objectbox.ui.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.objectbox.BoxStore

class UserViewModelFactory(private val store: BoxStore) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
      @Suppress("UNCHECKED_CAST")
      return UserViewModel(store) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}