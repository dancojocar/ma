package com.example.ma.sm.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ma.sm.StockApp;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.net.ServerNotifier;
import com.example.ma.sm.task.CancellableCall;
import com.example.ma.sm.task.listeners.OnErrorListener;
import com.example.ma.sm.task.listeners.OnErrorUpdateListener;
import com.example.ma.sm.task.listeners.OnPortfolioUpdateListener;
import com.example.ma.sm.task.listeners.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class StockManager {
  private static final String TAG = StockManager.class.getSimpleName();
  @Inject
  ClientConnection client;
  private OnPortfolioUpdateListener onUpdateListener;
  private ServerNotifier serverNotifier;
  private OnErrorUpdateListener onErrorListener;
  private CancellableCall cc;
  private List<Portfolio> portfolios;

  public StockManager(Context context) {
    StockApp app = (StockApp) context;
    app.injector().inject(this);
    portfolios = new ArrayList<>();
  }

  public void addPortfolio(String portfolioName) {
    Portfolio p = new Portfolio(portfolioName);
    portfolios.add(p);
    updateListeners();
    pushToServer(p);
  }

  private void pushToServer(final Portfolio p) {
    if (serverNotifier != null) {
      Thread th = new Thread(new Runnable() {
        @Override
        public void run() {
          serverNotifier.push(p);
        }
      });
      th.start();
    }
  }

  @NonNull
  private Portfolio getDummyPortfolio() {
    return new Portfolio("Test: " + System.currentTimeMillis());
  }

  private void updateListeners() {
    if (onUpdateListener != null)
      onUpdateListener.updated();
  }

  public List<Portfolio> getPortfolios() {
    return portfolios;
  }

  private void loadPortfolios(List<Portfolio> portfolios) {
    this.portfolios = portfolios;
  }

  public void setOnUpdateListener(OnPortfolioUpdateListener listener) {
    this.onUpdateListener = listener;
  }

  public void setOnErrorUpdateListener(OnErrorUpdateListener listener) {
    this.onErrorListener = listener;
  }

  public void setServerListener(ServerNotifier listener) {
    this.serverNotifier = listener;
  }

  public void delete() {
    portfolios = new ArrayList<>();
    updateListeners();
  }

  public void fetchData() {
    if (cc == null) {
      onUpdateListener.preUpdate();
      cc = client.getPortfolios(new OnSuccessListener<List<Portfolio>>() {

        @Override
        public void onSuccess(final List<Portfolio> portfolios) {
          Log.v(TAG, "getPortfolios: onSuccess");
          loadPortfolios(portfolios);
          updateListeners();
          cc = null;
        }
      }, new OnErrorListener() {

        @Override
        public void onError(Exception e) {
          Log.e(TAG, "getPortfolios: onError: " + e.getMessage());
          // cancel progress bar
          if (cc != null)
            cc.cancel();
          if (onErrorListener != null)
            onErrorListener.onError(e);
          cc = null;
        }
      });
    }
  }

  public void cancelCall() {
    if (cc != null) {
      Log.v(TAG, "canceling call");
      cc.cancel();
    } else {
      if (onErrorListener != null)
        onErrorListener.onError(new RuntimeException("no call to cancel"));
    }

  }

  public void genDummyData() {
    for (int i = 0; i < 5; i++)
      portfolios.add(getDummyPortfolio());
    updateListeners();
  }


}
