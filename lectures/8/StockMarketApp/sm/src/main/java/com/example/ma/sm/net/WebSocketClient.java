package com.example.ma.sm.net;

import android.content.Context;

import com.example.ma.sm.R;
import com.example.ma.sm.service.StockManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import timber.log.Timber;

import static okhttp3.WebSocket.TEXT;

public final class WebSocketClient implements WebSocketListener {
  private final ExecutorService writeExecutor = Executors.newSingleThreadExecutor();
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
    client.newWebSocketCall(request).enqueue(this);

    // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
    client.dispatcher().executorService().shutdown();
  }

  @Override
  public void onOpen(final WebSocket webSocket, Response response) {
    this.ws = webSocket;
    writeExecutor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          webSocket.message(RequestBody.create(TEXT, "Hello from app"));
        } catch (IOException e) {
          Timber.e(e, "error while opening the connection");
        }
      }
    });
  }

  public void close(String reason) {
    if (ws != null) {
      try {
        ws.close(1000, reason);
      } catch (IOException e) {
        Timber.e(e, "Unable to close the ws connection");
      }
    }
  }

  @Override
  public void onFailure(Throwable e, Response response) {
    Timber.e(e, "error while opening the connection");
    writeExecutor.shutdown();
  }

  @Override
  public void onMessage(ResponseBody message) throws IOException {
    if (message.contentType() == TEXT) {
      String text = message.string();
      if ("append".equalsIgnoreCase(text)) {
        Timber.v("request data append ");
        manager.fetchData();
      } else if ("clear".equalsIgnoreCase(text) || "clean".equalsIgnoreCase(text)) {
        Timber.v("request cleanup local data ");
        manager.delete();
      }
      Timber.v("MESSAGE: %s", text);
    } else {
      Timber.v("MESSAGE: %s", message.source().readByteString().hex());
    }
    message.close();
  }

  @Override
  public void onPong(ByteString payload) {
    Timber.v("PONG: %s", payload.utf8());
  }

  @Override
  public void onClose(int code, String reason) {
    Timber.v("CLOSE: %d reason: %s", code, reason);
    writeExecutor.shutdown();
  }
}
