package com.example.ma.sm;

import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.net.StockRestConnection;
import com.example.ma.sm.service.StockManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.realm.Realm;
import timber.log.Timber;

@Module
public class AppModule {
  private StockApp app;

  public AppModule(StockApp app) {
    this.app = app;
    Timber.v("appModule");
  }

  @Provides
  @Singleton
  StockManager provideStockManager() {
    Timber.v("create stock Manager");
    return new StockManager(app);
  }

  @Provides
  @Singleton
  ClientConnection provideClient() {
    Timber.v("create stock rest client");
    return new StockRestConnection(app);
  }

  @Provides
  Realm provideRealm() {
    return Realm.getDefaultInstance();
  }

}
