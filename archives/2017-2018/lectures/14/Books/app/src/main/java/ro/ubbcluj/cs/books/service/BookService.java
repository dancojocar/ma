package ro.ubbcluj.cs.books.service;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import ro.ubbcluj.cs.books.domain.Book;
import rx.Observable;

public interface BookService {
  String SERVICE_ENDPOINT = "http://192.168.2.1:3000";


  @GET("books")
  Observable<List<Book>> getBooks();

  @POST("book")
  Observable<Book> addBook(@Body Book e);

}
