package ro.ubbcluj.cs.books.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.books.BookApp
import ro.ubbcluj.cs.books.domain.Book
import ro.ubbcluj.cs.books.service.BookService
import ro.ubbcluj.cs.books.service.ServiceFactory

class MainModel : ViewModel() {

  private val service: BookService = ServiceFactory
      .createRetrofitService(BookService::class.java, BookService.SERVICE_ENDPOINT)

  private val mutableBooks = MutableLiveData<List<Book>>().apply { value = emptyList() }
  private val mutableLoading = MutableLiveData<Boolean>().apply { value = false }
  private val mutableMessage = MutableLiveData<String>()

  val books: LiveData<List<Book>> = mutableBooks
  val loading: LiveData<Boolean> = mutableLoading
  val message: LiveData<String> = mutableMessage

  fun fetchBooksFromNetwork(app: BookApp) {
    mutableLoading.value = true
    viewModelScope.launch {
      try {
        mutableBooks.value = service.getBooks()
        launch(Dispatchers.IO) {
          app.db.bookDao.deleteBooks()
          app.db.bookDao.addBooks(books.value!!)
        }
      } catch (e: Exception) {
        mutableMessage.value = "Received an error while retrieving the data: ${e.message}"
      } finally {
        mutableLoading.value = false
      }
    }
  }


  fun fetchBooks(app: BookApp) {
    mutableLoading.value = true
    try {
      GlobalScope.launch(Dispatchers.IO) {
        val numberOfBooks = app.db.bookDao.numberOfBooks
        if (numberOfBooks <= 0) {
          fetchBooksFromNetwork(app)
        }
      }
    } catch (e: Exception) {
      mutableMessage.value = "Received an error while retrieving local data: ${e.message}"
    } finally {
      mutableLoading.value = false
    }
  }

  fun addBook(app: BookApp, book: Book) {
    mutableLoading.value = true
    viewModelScope.launch {
      try {
        service.addBook(book)
        launch(Dispatchers.IO) {
          app.db.bookDao.addBook(book)
        }
      } catch (e: Exception) {
        mutableMessage.value = "Received an error while adding the data: ${e.message}"
      } finally {
        mutableLoading.value = false
      }
    }
  }
}