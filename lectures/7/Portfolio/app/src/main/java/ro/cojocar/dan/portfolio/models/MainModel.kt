package ro.cojocar.dan.portfolio.models

import android.util.SparseArray
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ro.cojocar.dan.portfolio.domain.Portfolio
import ro.cojocar.dan.portfolio.getFreshPortfolios
import ro.cojocar.dan.portfolio.logd
import ro.cojocar.dan.portfolio.repository.NetworkRepository
import ro.cojocar.dan.portfolio.service.LoginCredentials


class MainModel : ViewModel() {
  private var authToken: String? = null
  private val cache = SparseArray<List<Portfolio>>(10)

  private val mutablePortfolios = MutableLiveData<List<Portfolio>>().apply { value = emptyList() }
  private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
  private val mutableMessage = MutableLiveData<String>()

  val portfolios: LiveData<List<Portfolio>> = mutablePortfolios
  val loading: LiveData<Boolean> = mutableLoading
  val message: LiveData<String> = mutableMessage


  fun fetchData() {
    viewModelScope.launch {
      mutableLoading.value = true
      try {
        mutablePortfolios.value =
          cache.getFreshPortfolios(0) ?: getNewPortfolios(0)
      } catch (e: Exception) {
        mutableMessage.value = "Received an error while retrieving the data: ${e.message}"
      } finally {
        mutableLoading.value = false
      }
    }
  }

  suspend fun auth(): Boolean {
    if (authToken == null || authToken?.isEmpty() == true) {
      authToken = NetworkRepository.auth(LoginCredentials("test", "test1"))
      NetworkRepository.setToken(authToken)
    }
    return authToken?.isNotEmpty() ?: false
  }

  /**
   * @throws IllegalStateException
   */
  private suspend fun getNewPortfolios(userId: Int) = withContext(Dispatchers.IO) {
    logd("Fetch from network")
    NetworkRepository.getPortfolios().also {
      cache.put(userId, it)
    }
  }
}