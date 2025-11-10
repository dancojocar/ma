package ro.cojocar.dan.portfolio.models

import android.util.SparseArray
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

  private val _portfolios = MutableStateFlow<List<Portfolio>>(emptyList())
  val portfolios: StateFlow<List<Portfolio>> = _portfolios.asStateFlow()

  private val _loading = MutableStateFlow(false)
  val loading: StateFlow<Boolean> = _loading.asStateFlow()

  private val _message = MutableStateFlow<String?>(null)
  val message: StateFlow<String?> = _message.asStateFlow()


  fun fetchData() {
    viewModelScope.launch {
      _loading.value = true
      try {
        _portfolios.value =
          cache.getFreshPortfolios(0) ?: getNewPortfolios(0)
        _message.value = "Data loaded successfully"
      } catch (e: java.net.ConnectException) {
        _message.value = "Server is not available. Please check your connection and ensure the server is running."
        logd("Connection failed: ${e.message}")
      } catch (e: java.net.SocketTimeoutException) {
        _message.value = "Request timed out. Please try again."
        logd("Timeout: ${e.message}")
      } catch (e: Exception) {
        _message.value = "Error retrieving data: ${e.message}"
        logd("Error: ${e.message}")
      } finally {
        _loading.value = false
      }
    }
  }

  suspend fun auth(): Boolean {
    return try {
      if (authToken == null || authToken?.isEmpty() == true) {
        authToken = NetworkRepository.auth(LoginCredentials("test", "test1"))
        NetworkRepository.setToken(authToken)
      }
      authToken?.isNotEmpty() ?: false
    } catch (e: java.net.ConnectException) {
      _message.value = "Server is not available. Please check your connection and ensure the server is running at http://10.0.2.2:8080"
      logd("Connection failed: ${e.message}")
      false
    } catch (e: Exception) {
      _message.value = "Authentication failed: ${e.message}"
      logd("Auth error: ${e.message}")
      false
    }
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