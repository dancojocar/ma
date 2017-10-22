package com.example.ma.sm;

import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.service.StockManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface Injector {
  void inject(StockApp app);

  void inject(StockManager manager);

  void inject(ClientConnection restClient);
}
