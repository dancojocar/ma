package ro.ubbcluj.cs.books.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters

import ro.ubbcluj.cs.books.domain.Book

@Database(entities = [Book::class], version = 1)
@TypeConverters(Converters::class)
abstract class BookDatabase : RoomDatabase() {
  abstract val bookDao: BooksDao
}
