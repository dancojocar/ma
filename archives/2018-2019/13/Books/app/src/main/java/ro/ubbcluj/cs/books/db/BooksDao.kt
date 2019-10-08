package ro.ubbcluj.cs.books.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update

import ro.ubbcluj.cs.books.domain.Book

@Dao
interface BooksDao {

  @get:Query("select * from books")
  val books: LiveData<MutableList<Book>>

  @Insert
  fun addBook(book: Book)

  @Insert
  fun addBooks(books: List<Book>)

  @Delete
  fun deleteBook(b: Book)

  @Query("delete from books")
  fun deleteBooks()

  @Update
  fun updateBook(b: Book)
}
