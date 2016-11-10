package com.example.ma.sm;

import android.app.Application;

import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.service.StockManager;

import javax.inject.Inject;

import timber.log.Timber;

import static timber.log.Timber.DebugTree;

public class StockApp extends Application {
  @Inject
  StockManager manager;
  @Inject
  ClientConnection client;
  private Injector injector;

  @Override
  public void onCreate() {
    super.onCreate();
    injector = DaggerInjector.builder()
        .appModule(new AppModule(this)).build();
    injector.inject(this);
    manager.setServerListener(client);
    if (BuildConfig.DEBUG) {
      Timber.plant(new DebugTree());
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
}
