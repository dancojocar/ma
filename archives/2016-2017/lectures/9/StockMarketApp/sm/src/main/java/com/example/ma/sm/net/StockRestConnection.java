package com.example.ma.sm.net;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.example.ma.sm.R;
import com.example.ma.sm.StockApp;
import com.example.ma.sm.json.reader.PortfolioReader;
import com.example.ma.sm.json.reader.ResourceListReader;
import com.example.ma.sm.json.reader.SymbolReader;
import com.example.ma.sm.json.reader.TokenReader;
import com.example.ma.sm.json.writer.PortfolioWriter;
import com.example.ma.sm.json.writer.UserWriter;
import com.example.ma.sm.model.Portfolio;
import com.example.ma.sm.model.User;
import com.example.ma.sm.task.CancellableCall;
import com.example.ma.sm.task.listeners.OnErrorListener;
import com.example.ma.sm.task.listeners.OnSuccessListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

public class StockRestConnection implements ClientConnection, ServerNotifier {
  public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
  private OkHttpClient client;
  private String url;
  private String authUrl;

  public StockRestConnection(Context context) {
    StockApp app = (StockApp) context;
    app.injector().inject(this);
    client = new OkHttpClient();
    url = app.getString(R.string.serverConnectionUrl) + "/p";
    authUrl = app.getString(R.string.serverConnectionUrl) + "/token-auth";
  }

  public CancellableCall getPortfolios(User user, final OnSuccessListener<List<Portfolio>> onSuccessListener,
                                       final OnErrorListener onErrorListener) {
    Timber.v("getPortfolios");
    Request.Builder builder = new Request.Builder();
    builder.url(url);
    builder.header("Authorization", String.format("Bearer %s", user.getToken()));
    final Request request = builder.build();
    Call call = null;
    try {
      call = client.newCall(request);
      Timber.v("queue request");
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          Timber.e(e, "received an error while retrieving the portfolios");
          onErrorListener.onError(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          Timber.v("received a response");
          if (response.code() == 200) {
            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
            onSuccessListener.onSuccess(new ResourceListReader<>(new PortfolioReader(new SymbolReader())).read(reader));
          } else
            onErrorListener.onError(new RuntimeException("Received a bad response"));
        }
      });
    } catch (Exception e) {
      Timber.e(e, "Error while connecting to the remote");
      onErrorListener.onError(e);
    }
    return new CancellableCall(call);
  }

  public CancellableCall login(User user, final OnSuccessListener<String> onSuccessListener,
                               final OnErrorListener onErrorListener) {
    Timber.v("login");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    JsonWriter writer;
    try {
      writer = new JsonWriter(new OutputStreamWriter(baos, "UTF-8"));
      new UserWriter().write(user, writer);
      writer.close();
    } catch (Exception e) {
      onErrorListener.onError(new RuntimeException("Failed to marshall the user"));
      return null;
    }

    Request.Builder builder = new Request.Builder();
    builder.url(authUrl);
    builder.post(RequestBody.create(JSON, baos.toByteArray()));
    final Request request = builder.build();
    Call call = null;
    try {
      call = client.newCall(request);
      Timber.v("queue request");
      call.enqueue(new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          Timber.e(e, "received an error while authenticating");
          onErrorListener.onError(e);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
          Timber.v("received a response");
          if (response.code() == 200) {
            JsonReader reader = new JsonReader(new InputStreamReader(response.body().byteStream(), "UTF-8"));
            onSuccessListener.onSuccess(new TokenReader().read(reader));
          } else
            onErrorListener.onError(new RuntimeException("Received a bad response"));
        }
      });
    } catch (Exception e) {
      Timber.e(e, "Error while trying to login");
      onErrorListener.onError(e);
    }
    return new CancellableCall(call);
  }


  @Override
  public void push(Portfolio p, final OnErrorListener onErrorListener) {
    Timber.v("pushPortfolio");
    try {
      StringWriter sw = new StringWriter();
      JsonWriter writer = new JsonWriter(sw);
      new PortfolioWriter().write(p, writer);
      String json = sw.toString();
      RequestBody body = RequestBody.create(JSON, json);
      Request request = new Request.Builder()
          .url(url)
          .post(body)
          .build();
      Response response = client.newCall(request).execute();
      Timber.v("response: %s", response);
    } catch (IOException e) {
      onErrorListener.onError(new IOException("Not able to push to remote service", e));
    }
  }
}
