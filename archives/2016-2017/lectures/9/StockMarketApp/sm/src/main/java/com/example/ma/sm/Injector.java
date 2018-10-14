package com.example.ma.sm;

import com.example.ma.sm.fragments.NewSymbolFragment;
import com.example.ma.sm.fragments.PortfolioDetailFragment;
import com.example.ma.sm.fragments.PortfolioFragment;
import com.example.ma.sm.fragments.SymbolDetailsFragment;
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

  void inject(PortfolioFragment fragment);

  void inject(PortfolioDetailFragment fragment);

  void inject(SymbolDetailsFragment fragment);

  void inject(NewSymbolFragment newSymbol);
}
