package com.example.ma.sm.net;

import android.content.Context;
import android.util.Log;

import com.example.ma.sm.R;
import com.example.ma.sm.service.StockManager;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public final class WebSocketClient extends WebSocketListener {
  private static final String TAG = WebSocketClient.class.getSimpleName();
  private WebSocket ws;
  private StockManager manager;

  public WebSocketClient(StockManager manager) {
    this.manager = manager;
  }

  public void connect(Context ctx) {
    OkHttpClient client = new OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build();

    Request request = new Request.Builder()
        .url(ctx.getString(R.string.wsConnectionUrl))
        .build();
    client.newWebSocket(request, this);

    // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
    client.dispatcher().executorService().shutdown();
    Log.i(TAG, "Client connected");
  }

  @Override
  public void onOpen(final WebSocket webSocket, Response response) {
    this.ws = webSocket;
    webSocket.send("Hello from app");
    webSocket.send("Use: ");
    webSocket.send(" - append - to trigger an append of portfolios");
    webSocket.send(" - clear - to clear all the portfolios");
  }

  public void close(String reason) {
    if (ws != null) {
      ws.close(1000, reason);
    }
  }

  @Override
  public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
    Log.e(TAG, "error while opening the connection", t);
  }

  @Override
  public void onMessage(WebSocket webSocket, String text) {
    if ("append".equalsIgnoreCase(text)) {
      Log.v(TAG, "request data append ");
      manager.fetchData();
    } else if ("clear".equalsIgnoreCase(text) || "clean".equalsIgnoreCase(text)) {
      Log.v(TAG, "request cleanup local data ");
      manager.delete();
    } else if ("fetch".equalsIgnoreCase(text)) {
      Log.v(TAG, "re-init");
      manager.delete();
      manager.fetchData();
    }
    Log.v(TAG, "MESSAGE: " + text);
  }

  @Override
  public void onClosing(WebSocket webSocket, int code, String reason) {
    Log.v(TAG, "onClosing: " + code + " " + reason);
  }

  @Override
  public void onClosed(WebSocket webSocket, int code, String reason) {
    Log.v(TAG, "onClosed: " + code + " " + reason);
  }
}
