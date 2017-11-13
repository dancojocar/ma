package com.example.ma.sm.net;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.ma.sm.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Google Sheets API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class GoogleSheetsRequestTask extends AsyncTask<Void, Void, List<String>> {
  private WeakReference<Activity> activity;
  private com.google.api.services.sheets.v4.Sheets service = null;
  private Exception lastError = null;
  private WeakReference<TextView> textOutput;
  private ProgressDialog progress;

  private static final int REQUEST_AUTHORIZATION = 1001;

  public GoogleSheetsRequestTask(Activity activity, GoogleAccountCredential credential,
                                 TextView tv, ProgressDialog progress) {
    this.activity = new WeakReference<>(activity);
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    service = new com.google.api.services.sheets.v4.Sheets.Builder(
        transport, jsonFactory, credential)
        .setApplicationName("PortfolioApp")
        .build();
    this.textOutput = new WeakReference<>(tv);
    this.progress = progress;
  }

  /**
   * Background task to call Google Sheets API.
   *
   * @param params no parameters needed for this task.
   */
  @Override
  protected List<String> doInBackground(Void... params) {
    try {
      return getDataFromApi();
    } catch (Exception e) {
      lastError = e;
      cancel(true);
      return null;
    }
  }

  /**
   * Fetch a list of names and majors of students in a sample spreadsheet:
   * https://docs.google.com/spreadsheets/d/1d2cGX1jJhjzWQrU8UONUzg6Gd7R6LNqEHfcPUzb5L1I/edit
   *
   * @return List of names and majors
   * @throws IOException when trying to access the document
   */
  private List<String> getDataFromApi() throws IOException {
    String spreadsheetId = "1d2cGX1jJhjzWQrU8UONUzg6Gd7R6LNqEHfcPUzb5L1I";
    String range = "Class Data!A2:E20";
    List<String> results = new ArrayList<>();
    ValueRange response = this.service.spreadsheets().values()
        .get(spreadsheetId, range)
        .execute();
    List<List<Object>> values = response.getValues();
    if (values != null) {
      results.add("Name, Major");
      for (List row : values) {
        results.add(row.get(0) + ", " + row.get(4));
      }
    }
    return results;
  }


  @Override
  protected void onPreExecute() {
    textOutput.get().setText("");
    progress.show();
  }

  @Override
  protected void onPostExecute(List<String> output) {
    progress.dismiss();
    if (output == null || output.size() == 0) {
      textOutput.get().setText(R.string.noResultMessage);
    } else {
      output.add(0, activity.get().getString(R.string.dataHeader));
      textOutput.get().setText(TextUtils.join("\n", output));
    }
  }

  @Override
  protected void onCancelled() {
    progress.dismiss();
    if (lastError != null) {
      if (lastError instanceof UserRecoverableAuthIOException) {
        activity.get().startActivityForResult(
            ((UserRecoverableAuthIOException) lastError).getIntent(),
            REQUEST_AUTHORIZATION);
      } else {
        textOutput.get().setText(activity.get().getString(R.string.errorOnCancel,
            lastError.getMessage()));
      }
    } else {
      textOutput.get().setText(R.string.cancelRequestMessage);
    }
  }
}
