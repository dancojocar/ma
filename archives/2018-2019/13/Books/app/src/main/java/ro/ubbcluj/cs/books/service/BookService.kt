package ro.ubbcluj.cs.books.service

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ro.ubbcluj.cs.books.domain.Book
import rx.Observable

interface BookService {

  @get:GET("books")
  val books: Observable<List<Book>>

  @POST("book")
  fun addBook(@Body e: Book): Observable<Book>

  companion object {
    const val SERVICE_ENDPOINT = "http://10.0.2.2:3000"
  }
}
