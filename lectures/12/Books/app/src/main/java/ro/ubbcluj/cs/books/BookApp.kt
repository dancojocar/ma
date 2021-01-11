package ro.ubbcluj.cs.books

import android.app.Application
import androidx.room.Room

import ro.ubbcluj.cs.books.db.BookDatabase

class BookApp : Application() {
  lateinit var db: BookDatabase

  override fun onCreate() {
    super.onCreate()
    db = Room.databaseBuilder(applicationContext,
        BookDatabase::class.java, "database-name").build()
  }
}
