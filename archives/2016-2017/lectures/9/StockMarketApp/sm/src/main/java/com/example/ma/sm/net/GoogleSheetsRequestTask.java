package com.example.ma.sm.net;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.TextView;

import com.example.ma.sm.oauth.GoogleSheetAPI;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Google Sheets API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class GoogleSheetsRequestTask extends AsyncTask<Void, Void, List<String>> {
  static final int REQUEST_AUTHORIZATION = 1001;
  private GoogleSheetAPI googleSheetAPI;
  private com.google.api.services.sheets.v4.Sheets service = null;
  private Exception lastError = null;
  private TextView textOutput;
  private ProgressDialog progress;

  public GoogleSheetsRequestTask(GoogleSheetAPI googleSheetAPI, GoogleAccountCredential credential,
                                 TextView tv, ProgressDialog progress) {
    this.googleSheetAPI = googleSheetAPI;
    HttpTransport transport = AndroidHttp.newCompatibleTransport();
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    service = new com.google.api.services.sheets.v4.Sheets.Builder(
        transport, jsonFactory, credential)
        .setApplicationName("PortfolioApp")
        .build();
    this.textOutput = tv;
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
   * https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
   *
   * @return List of names and majors
   * @throws IOException
   */
  private List<String> getDataFromApi() throws IOException {
    String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
    String range = "Class Data!A2:E20";
    List<String> results = new ArrayList<String>();
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
    textOutput.setText("");
    progress.show();
  }

  @Override
  protected void onPostExecute(List<String> output) {
    progress.dismiss();
    if (output == null || output.size() == 0) {
      textOutput.setText("No results returned.");
    } else {
      output.add(0, "Data retrieved using the Google Sheets API:");
      textOutput.setText(TextUtils.join("\n", output));
    }
  }

  @Override
  protected void onCancelled() {
    progress.dismiss();
    if (lastError != null) {
      if (lastError instanceof UserRecoverableAuthIOException) {
        googleSheetAPI.startActivityForResult(
            ((UserRecoverableAuthIOException) lastError).getIntent(),
            REQUEST_AUTHORIZATION);
      } else {
        textOutput.setText("The following error occurred:\n"
            + lastError.getMessage());
      }
    } else {
      textOutput.setText("Request cancelled.");
    }
  }
}
