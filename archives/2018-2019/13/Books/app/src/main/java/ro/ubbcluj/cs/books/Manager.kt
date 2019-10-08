package ro.ubbcluj.cs.books


import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import android.widget.ProgressBar

import ro.ubbcluj.cs.books.domain.Book
import ro.ubbcluj.cs.books.service.BookService
import ro.ubbcluj.cs.books.service.ServiceFactory
import ro.ubbcluj.cs.books.utils.logd
import ro.ubbcluj.cs.books.utils.loge
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

internal class Manager(val app: BookApp) {
  private val service: BookService = ServiceFactory
      .createRetrofitService(BookService::class.java, BookService.SERVICE_ENDPOINT)


  fun networkConnectivity(context: Context): Boolean {
    val cm = context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnected
  }

  fun loadEvents(progressBar: ProgressBar, callback: MyCallback) {

    service.books
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(object : Subscriber<List<Book>>() {
          override fun onCompleted() {
            logd("Book Service completed")
            progressBar.visibility = View.GONE
          }

          override fun onError(e: Throwable) {
            loge("Error while loading the events", e)
            callback.showError("Not able to retrieve the data. Displaying local data!")
          }

          override fun onNext(books: List<Book>) {
            Thread(Runnable {
              app.db.bookDao.deleteBooks()
              app.db.bookDao.addBooks(books)
            }).start()
            logd("Books persisted")
          }
        })
  }

  fun save(book: Book, callback: MyCallback) {
    service.addBook(book)
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(object : Subscriber<Book>() {
          override fun onCompleted() {
            addDataLocally(book)
            logd("Book Service completed")
            callback.clear()
          }

          override fun onError(e: Throwable) {
            loge("Error while persisting an book", e)
            callback.showError("Not able to connect to the server, will not persist!")
          }

          override fun onNext(book: Book) {
            logd("Book persisted")
          }
        })
  }

  private fun addDataLocally(book: Book) {
    Thread(Runnable { app.db.bookDao.addBook(book) }).start()
  }
}
