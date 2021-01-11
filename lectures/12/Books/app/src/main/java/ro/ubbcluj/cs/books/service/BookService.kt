package ro.ubbcluj.cs.books.service

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import ro.ubbcluj.cs.books.domain.Book

interface BookService {

  @GET("/books")
  suspend fun getBooks(): List<Book>

  @POST("/book")
  suspend fun addBook(@Body e: Book): Book

  companion object {
    const val SERVICE_ENDPOINT = "http://10.0.2.2:3000"
  }
}
