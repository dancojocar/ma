package com.example.ma.sm;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.example.ma.sm.database.Migration;
import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.service.StockManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import javax.annotation.Nullable;
import javax.inject.Inject;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class StockApp extends Application {

  private static StockApp instance;
  @Inject
  StockManager manager;
  @Inject
  ClientConnection client;
  private RefWatcher refWatcher;
  private Injector injector;

  public static StockApp get() {
    return instance;
  }

  public static RefWatcher getRefWatcher() {
    return StockApp.get().refWatcher;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    refWatcher = LeakCanary.install(this);
    instance = (StockApp) getApplicationContext();
    injector = DaggerInjector.builder()
        .appModule(new AppModule(this)).build();
    Realm.init(getApplicationContext());
    RealmConfiguration config = new RealmConfiguration.Builder()
        .name("default0.realm")
        .schemaVersion(1)
        .build();

    // You can then manually call Realm.migrateRealm().
    try {
      Realm.migrateRealm(config, new Migration());
    } catch (Exception ignored) {
      // If the Realm file doesn't exist, just ignore.
    }

    Realm realm = null;
    try {
      Realm.setDefaultConfiguration(config);
      realm = Realm.getDefaultInstance();
    } catch (RealmMigrationNeededException e) {
      Realm.deleteRealm(config);
      //Realm file has been deleted.
      Realm.setDefaultConfiguration(config);
    } finally {
      if (realm != null)
        realm.close();
    }
    injector.inject(this);
    manager.setServerListener(client);

    // https://www.fabric.io/kits/android/crashlytics/summary
    CrashlyticsCore core = new CrashlyticsCore.Builder()
        .disabled(BuildConfig.DEBUG)
        .build();
    Fabric.with(this, new Crashlytics.Builder().core(core).build());
    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
    } else {
      Timber.plant(new CrashlyticsTree());
    }
    Timber.v("onCreate");
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    if (manager != null)
      manager.cancelCall();
    Timber.v("onTerminate done");
  }

  public StockManager getManager() {
    Timber.v("getManager done");
    return manager;
  }

  public Injector injector() {
    return injector;
  }

  public class CrashlyticsTree extends Timber.Tree {
    private static final String CRASHLYTICS_KEY_PRIORITY = "priority";
    private static final String CRASHLYTICS_KEY_TAG = "tag";
    private static final String CRASHLYTICS_KEY_MESSAGE = "message";

    @Override
    protected void log(int priority, @Nullable String tag, @Nullable String message, @Nullable Throwable throwable) {
      if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
        return;
      }

      Crashlytics.setInt(CRASHLYTICS_KEY_PRIORITY, priority);
      Crashlytics.setString(CRASHLYTICS_KEY_TAG, tag);
      Crashlytics.setString(CRASHLYTICS_KEY_MESSAGE, message);

      if (throwable == null) {
        Crashlytics.logException(new Exception(message));
      } else {
        Crashlytics.logException(throwable);
      }
    }
  }
}
