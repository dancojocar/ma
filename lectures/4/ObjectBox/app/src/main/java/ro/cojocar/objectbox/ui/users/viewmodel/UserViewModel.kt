package ro.cojocar.objectbox.ui.users.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.objectbox.BoxStore
import io.objectbox.kotlin.flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ro.cojocar.objectbox.domain.User

class UserViewModel(private val store: BoxStore) : ViewModel() {

  private val userDao = store.boxFor(User::class.java)
  private val _users = MutableLiveData<List<User>>()
  val users: LiveData<List<User>> = _users

  init {
    prepopulateUsers()
    subscribeToUsers()
  }

  private fun prepopulateUsers() {
    if (userDao.count() == 0L) {
      val sampleUsers = listOf(
        User(name = "Alice", email = "alice@example.com"),
        User(name = "Bob", email = "bob@example.com")
      )
      userDao.put(sampleUsers)
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun subscribeToUsers() {
    viewModelScope.launch {
      userDao.query().build().flow().collect { users ->
        _users.postValue(users)
      }
    }
  }

  fun addUser(name: String, email: String) {
    viewModelScope.launch {
      val newUser = User(name = name, email = email)
      userDao.put(newUser)
    }
  }
}
