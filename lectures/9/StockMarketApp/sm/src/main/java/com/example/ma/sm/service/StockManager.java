package com.example.ma.sm.service;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.ma.sm.StockApp;
import com.example.ma.sm.database.DBContract;
import com.example.ma.sm.helper.UserHelper;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.model.Symbol;
import com.example.ma.sm.model.User;
import com.example.ma.sm.net.ClientConnection;
import com.example.ma.sm.net.ServerNotifier;
import com.example.ma.sm.net.WebSocketClient;
import com.example.ma.sm.provider.PortfolioContentProvider;
import com.example.ma.sm.provider.UserContentProvider;
import com.example.ma.sm.task.CancellableCall;
import com.example.ma.sm.task.listeners.OnErrorListener;
import com.example.ma.sm.task.listeners.OnErrorUpdateListener;
import com.example.ma.sm.task.listeners.OnSuccessListener;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmList;
import timber.log.Timber;

public class StockManager {
  private final StockApp app;
  @Inject
  ClientConnection client;
  @Inject
  Realm realm;
  private ServerNotifier serverNotifier;
  private OnErrorUpdateListener onErrorListener;
  private CancellableCall cc;
  private ContentResolver resolver;
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
    realm.beginTransaction();
    realm.delete(Portfolio.class);
    realm.commitTransaction();
  }

  public void fetchData() {
    if (cc == null && user != null) {
      cc = client.getPortfolios(user, new OnSuccessListener<List<Portfolio>>() {

        @Override
        public void onSuccess(final List<Portfolio> portfolios) {
          Timber.v("getPortfolios: onSuccess");
          loadPortfolios(portfolios);
          cc = null;
        }
      }, new OnErrorListener() {

        @Override
        public void onError(Exception e) {
          Timber.e(e, "getPortfolios: onError ");
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
          Timber.v("login: onSuccess");
          user.setToken(token);
          resolver.insert(UserContentProvider.CONTENT_URI, UserHelper.fromUser(user));
          cc = null;
        }
      }, new OnErrorListener() {

        @Override
        public void onError(Exception e) {
          Timber.e(e, "login: onError");
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

  public void addPortfolio(Portfolio p) {
    realm.beginTransaction();
    Number id = realm.where(Portfolio.class).max("id");
    int nextID = 1;
    if (id != null)
      nextID = id.intValue() + 1;
    Portfolio portfolio = realm.createObject(Portfolio.class, nextID);
    portfolio.setLastModified(p.getLastModified());
    portfolio.setName(p.getName());
    RealmList<Symbol> symbols = new RealmList<>();
    for (Symbol s : p.getSymbols()) {
      addSymbol(portfolio.getId(), s);
    }
    portfolio.setSymbols(symbols);
    realm.commitTransaction();
  }

  public void addSymbol(long portfolioId, Symbol s) {
    Number id = realm.where(Symbol.class).max("id");
    int nextID = 1;
    if (id != null) {
      nextID = id.intValue() + 1;
    }
    s.setId(nextID);
    s.setPortfolioId(portfolioId);
    realm.copyToRealmOrUpdate(s);
  }

  public void cancelCall() {
    if (cc != null) {
      Timber.v("canceling call");
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

  public void setRealm(Realm realm) {
    this.realm = realm;
  }
}
