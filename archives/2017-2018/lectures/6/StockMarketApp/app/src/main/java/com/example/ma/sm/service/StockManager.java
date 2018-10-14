package com.example.ma.sm.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.ma.sm.StockApp;
import com.example.ma.sm.database.DBContract;
import com.example.ma.sm.helper.PortfolioHelper;
import com.example.ma.sm.helper.SymbolHelper;
import com.example.ma.sm.helper.UserHelper;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.model.Symbol;
import com.example.ma.sm.model.User;
import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.net.ServerNotifier;
import com.example.ma.sm.net.WebSocketClient;
import com.example.ma.sm.provider.PortfolioContentProvider;
import com.example.ma.sm.provider.SymbolContentProvider;
import com.example.ma.sm.provider.UserContentProvider;
import com.example.ma.sm.task.CancellableCall;
import com.example.ma.sm.task.listeners.OnErrorListener;
import com.example.ma.sm.task.listeners.OnErrorUpdateListener;
import com.example.ma.sm.task.listeners.OnSuccessListener;

import java.util.List;

import javax.inject.Inject;

public class StockManager {
  private static final String TAG = StockManager.class.getSimpleName();
  @Inject
  ClientConnection client;
  private ServerNotifier serverNotifier;
  private OnErrorUpdateListener onErrorListener;
  private CancellableCall cc;
  private ContentResolver resolver;
  private final StockApp app;
  private User user;
  private WebSocketClient ws;

  public StockManager(Context context) {
    app = (StockApp) context;
    app.injector().inject(this);
    resolver = context.getContentResolver();
  }

  public void addPortfolio(String portfolioName) {
    Portfolio p = new Portfolio(portfolioName);
    addPortfolio(p);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
    if (prefs.getBoolean("network_preference", false))
      pushContentToServer(p);
  }

  private void pushContentToServer(final Portfolio p) {
    if (serverNotifier != null) {
      Thread th = new Thread(new Runnable() {
        @Override
        public void run() {
          serverNotifier.push(p, new OnErrorListener() {
            @Override
            public void onError(Exception e) {
              if (onErrorListener != null)
                onErrorListener.onError(e);
            }
          });
        }
      });
      th.start();
    }
  }


  public Cursor getPortfolios() {
    return resolver.query(PortfolioContentProvider.CONTENT_URI, null, null, null, null);
  }


  public void setOnErrorUpdateListener(OnErrorUpdateListener listener) {
    this.onErrorListener = listener;
  }

  public void setServerListener(ServerNotifier listener) {
    this.serverNotifier = listener;
  }

  public void delete() {
    resolver.delete(PortfolioContentProvider.CONTENT_URI, null, null);
  }

  public void fetchData() {
    if (cc == null && user != null) {
      cc = client.getPortfolios(user, new OnSuccessListener<List<Portfolio>>() {

        @Override
        public void onSuccess(final List<Portfolio> portfolios) {
          Log.v(TAG, "getPortfolios: onSuccess");
          loadPortfolios(portfolios);
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
    } else if (user == null)
      onErrorListener.onError(new RuntimeException("Authenticate first!"));
  }

  public void login(String username, String pass) {
    final ContentResolver resolver = app.getContentResolver();
    user = new User(username, pass);
    String[] projection = new String[]{
        DBContract.UserTable._ID,
        DBContract.UserTable.COLUMN_NAME,
        DBContract.UserTable.COLUMN_PASS,
        DBContract.UserTable.COLUMN_AUTH_CODE
    };
    String selection = DBContract.UserTable.COLUMN_NAME + " = ?";
    String[] selectionArgs = new String[]{username};
    String order = DBContract.UserTable._ID + " DESC ";
    Cursor cursor = resolver.query(UserContentProvider.CONTENT_URI, projection, selection, selectionArgs, order);
    if (cursor != null) {
      try {
        if (cursor.moveToFirst()) {
          user.setToken(cursor.getString(cursor.getColumnIndex(DBContract.UserTable.COLUMN_AUTH_CODE)));
        }
      } finally {
        cursor.close();
      }
    }
    if (cc == null && user.getToken() == null) {
      cc = client.login(user, new OnSuccessListener<String>() {

        @Override
        public void onSuccess(final String token) {
          Log.v(TAG, "login: onSuccess");
          user.setToken(token);
          resolver.insert(UserContentProvider.CONTENT_URI, UserHelper.fromUser(user));
          cc = null;
        }
      }, new OnErrorListener() {

        @Override
        public void onError(Exception e) {
          Log.e(TAG, "login: onError: " + e.getMessage());
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


  private void loadPortfolios(List<Portfolio> portfolios) {
    for (Portfolio p : portfolios)
      addPortfolio(p);
  }

  private void addPortfolio(Portfolio p) {
    Uri result = resolver.insert(PortfolioContentProvider.CONTENT_URI, PortfolioHelper.fromPortfolio(p));
    if (result != null) {
      long pid = Long.parseLong(result.getLastPathSegment());
      Log.v(TAG, "added portfolio with id: " + pid);
      for (Symbol s : p.getSymbols()) {
        Uri symbolResult = resolver.insert(SymbolContentProvider.CONTENT_URI, SymbolHelper.fromSymbol(s, pid));
        if (symbolResult != null)
          Log.v(TAG, "added symbol with id: " + symbolResult.getLastPathSegment());
      }
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
      addPortfolio(getDummyPortfolio());
  }

  @NonNull
  private Portfolio getDummyPortfolio() {
    return new Portfolio("Test: " + System.currentTimeMillis());
  }

  public void deleteTokens() {
    final ContentResolver resolver = app.getContentResolver();
    resolver.delete(UserContentProvider.CONTENT_URI, null, null);
  }

  public void connectToWS() {
    ws = new WebSocketClient(this);
    ws.connect(app);
  }

  public void disconnectFromWS() {
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        ws.close("user requested");
      }
    });
    t.start();
  }
}
