package com.example.ma.sm;

import android.app.Application;
import android.util.Log;

import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.service.StockManager;

import javax.inject.Inject;

public class StockApp extends Application {
  private static final String TAG = StockApp.class.getSimpleName();
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
    Log.v(TAG, "onCreate");
  }


  @Override
  public void onTerminate() {
    super.onTerminate();
    if (manager != null)
      manager.cancelCall();
    Log.v(TAG, "onTerminate done");
  }

  public StockManager getManager() {
    Log.v(TAG, "getManager done");
    return manager;
  }

  public Injector injector() {
    return injector;
  }
}
