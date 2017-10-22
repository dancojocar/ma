package com.example.ma.sm;

import android.util.Log;

import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.net.StockRestConnection;
import com.example.ma.sm.service.StockManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class AppModule {
  private static final String TAG = AppModule.class.getSimpleName();
  private StockApp app;

  AppModule(StockApp app) {
    this.app = app;
    Log.v(TAG, "appModule");
  }

  @Provides
  @Singleton
  StockManager provideStockManager() {
    Log.v(TAG, "create stock Manager");
    return new StockManager(app);
  }

  @Provides
  @Singleton
  ClientConnection provideClient() {
    Log.v(TAG, "create stock rest client");
    return new StockRestConnection(app);
  }

}
