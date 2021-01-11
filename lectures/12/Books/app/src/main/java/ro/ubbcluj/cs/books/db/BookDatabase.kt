package ro.ubbcluj.cs.books.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import ro.ubbcluj.cs.books.domain.Book

@Database(entities = [Book::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class BookDatabase : RoomDatabase() {
  abstract val bookDao: BooksDao
}
