package ro.ubbcluj.cs.books.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ro.ubbcluj.cs.books.domain.Book;

@Dao
public interface BooksDao {

  @Insert
  void addBook(Book book);

  @Insert
  void addBooks(List<Book> books);

  @Delete
  void deleteBook(Book b);

  @Query("delete from books")
  void deleteBooks();

  @Update
  void updateBook(Book b);

  @Query("select * from books")
  LiveData<List<Book>> getBooks();
}
