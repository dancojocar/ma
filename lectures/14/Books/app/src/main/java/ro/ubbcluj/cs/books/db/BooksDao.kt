package ro.ubbcluj.cs.books.db

import androidx.lifecycle.LiveData
import androidx.room.*
import ro.ubbcluj.cs.books.domain.Book

@Dao
interface BooksDao {

  @get:Query("select * from books")
  val books: LiveData<MutableList<Book>>

  @get:Query("select count(*) from books")
  val numberOfBooks: Int

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
