package ro.ubbcluj.cs.books;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

public class BookApp extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Timber.plant(new Timber.DebugTree());
    Realm.init(this);
    RealmConfiguration config = new RealmConfiguration.Builder().build();
    Realm.deleteRealm(config);
    Realm.setDefaultConfiguration(config);
  }
}
