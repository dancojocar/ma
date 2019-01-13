package ro.ubbcluj.cs.books

import android.app.Application
import android.arch.persistence.room.Room

import ro.ubbcluj.cs.books.db.BookDatabase

class BookApp : Application() {
  lateinit var db: BookDatabase

  override fun onCreate() {
    super.onCreate()
    db = Room.databaseBuilder<BookDatabase>(applicationContext,
        BookDatabase::class.java, "database-name").build()
  }
}
