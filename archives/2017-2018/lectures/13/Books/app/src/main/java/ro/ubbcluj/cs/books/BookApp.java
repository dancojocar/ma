package ro.ubbcluj.cs.books;

import android.app.Application;
import android.arch.persistence.room.Room;

import ro.ubbcluj.cs.books.db.BookDatabase;
import timber.log.Timber;

public class BookApp extends Application {
  public BookDatabase db;

  @Override
  public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
    db = Room.databaseBuilder(getApplicationContext(),
        BookDatabase.class, "database-name").build();
  }
}
