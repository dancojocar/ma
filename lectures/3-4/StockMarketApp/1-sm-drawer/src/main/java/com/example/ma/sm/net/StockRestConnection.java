package com.example.ma.sm.net;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.example.ma.sm.R;
import com.example.ma.sm.StockApp;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.reader.PortfolioReader;
import com.example.ma.sm.reader.ResourceListReader;
import com.example.ma.sm.reader.SymbolReader;
import com.example.ma.sm.task.CancellableCall;
import com.example.ma.sm.task.listeners.OnErrorListener;
import com.example.ma.sm.task.listeners.OnSuccessListener;
import com.example.ma.sm.writer.PortfolioWriter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StockRestConnection implements ClientConnection, ServerNotifier {
  public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private static final String TAG = StockRestConnection.class.getSimpleName();
  private OkHttpClient client;
  private String url;

  public StockRestConnection(Context context) {
    StockApp app = (StockApp) context;
    app.injector().inject(this);
    client = new OkHttpClient();
    url = app.getString(R.string.serverConnectionUrl);
  }

  public CancellableCall getPortfolios(final OnSuccessListener<List<Portfolio>> onSuccessListener,
                                       final OnErrorListener onErrorListener) {
    Log.v(TAG, "getPortfolios");
    Request request = new Request.Builder().url(url).build();
    Call call = null;
    try {
      call = client.newCall(request);
      Log.v(TAG, "queue request");
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          Log.e(TAG, "received an error: ", e);
          onErrorListener.onError(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          Log.v(TAG, "received a response");
          JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
          onSuccessListener.onSuccess(new ResourceListReader<Portfolio>(new PortfolioReader(new SymbolReader())).read(reader));
        }
      });
    } catch (Exception e) {
      Log.e(TAG, "Error while connecting to the remore", e);
      onErrorListener.onError(e);
    }
    return new CancellableCall(call);
  }

  @Override
  public void push(Portfolio p) {
    Log.v(TAG, "pushPortfolio");
    try {
      StringWriter sw = new StringWriter();
      JsonWriter writer = new JsonWriter(sw);
      new PortfolioWriter(writer).write(p);
      String json = sw.toString();
      RequestBody body = RequestBody.create(JSON, json);
      Request request = new Request.Builder()
          .url(url)
          .post(body)
          .build();
      Response response = client.newCall(request).execute();
      Log.v(TAG, "response: " + response);
    } catch (IOException e) {
      Log.v(TAG, "error while pushing the request", e);
    }
  }
}
