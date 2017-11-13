package com.example.ma.sm.oauth;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ma.sm.R;
import com.example.ma.sm.net.GoogleSheetsRequestTask;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * Enable the api using:
 * https://console.developers.google.com/start/api?id=sheets.googleapis.com
 * <p>
 * Following: https://developers.google.com/sheets/quickstart/android
 * <p>
 * keytool -exportcert -keystore ~/.android/debug.keystore -list -v
 */
public class GoogleSheetAPI extends Activity
    implements EasyPermissions.PermissionCallbacks {
  GoogleAccountCredential credentials;
  private TextView textOutput;
  private Button apiButton;
  ProgressDialog progress;

  static final int REQUEST_ACCOUNT_PICKER = 1000;
  static final int REQUEST_AUTHORIZATION = 1001;
  static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
  static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;

  public static final String PREF_ACCOUNT_NAME = "accountName";
  private static final String[] SCOPES = {SheetsScopes.SPREADSHEETS_READONLY};

  /**
   * Create the main activity.
   *
   * @param savedInstanceState previously saved instance data.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.auth_main);

    apiButton = findViewById(R.id.authButton);
    textOutput = findViewById(R.id.authMessage);

    apiButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        apiButton.setEnabled(false);
        textOutput.setText("");
        getResultsFromApi();
        apiButton.setEnabled(true);
      }
    });

    progress = new ProgressDialog(this);
    progress.setMessage(getString(R.string.progressGoogleSheetMessage));

    // Initialize credentials and service object.
    credentials = GoogleAccountCredential.usingOAuth2(
        getApplicationContext(), Arrays.asList(SCOPES))
        .setBackOff(new ExponentialBackOff());
  }


  /**
   * Attempt to call the API, after verifying that all the preconditions are
   * satisfied. The preconditions are: Google Play Services installed, an
   * account was selected and the device currently has online access. If any
   * of the preconditions are not satisfied, the app will prompt the user as
   * appropriate.
   */
  private void getResultsFromApi() {
    if (!isGooglePlayServicesAvailable()) {
      acquireGooglePlayServices();
    } else if (credentials.getSelectedAccountName() == null) {
      chooseAccount();
    } else if (!isDeviceOnline()) {
      textOutput.setText(R.string.noNetConnection);
    } else {
      new GoogleSheetsRequestTask(this, credentials, textOutput, progress).execute();
    }
  }

  /**
   * Attempts to set the account used with the API credentials. If an account
   * name was previously saved it will use that one; otherwise an account
   * picker dialog will be shown to the user. Note that the setting the
   * account to use with the credentials object requires the app to have the
   * GET_ACCOUNTS permission, which is requested here if it is not already
   * present. The AfterPermissionGranted annotation indicates that this
   * function will be rerun automatically whenever the GET_ACCOUNTS permission
   * is granted.
   */
  @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
  private void chooseAccount() {
    if (EasyPermissions.hasPermissions(
        this, Manifest.permission.GET_ACCOUNTS)) {
      SharedPreferences prefs =
          PreferenceManager.getDefaultSharedPreferences(this);
      String accountName = prefs.getString(PREF_ACCOUNT_NAME, null);
      if (accountName != null) {
        credentials.setSelectedAccountName(accountName);
        getResultsFromApi();
      } else {
        // Start a dialog from which the user can choose an account
        startActivityForResult(
            credentials.newChooseAccountIntent(),
            REQUEST_ACCOUNT_PICKER);
      }
    } else {
      // Request the GET_ACCOUNTS permission via a user dialog
      EasyPermissions.requestPermissions(
          this,
          "This app needs to access your Google account (via Contacts).",
          REQUEST_PERMISSION_GET_ACCOUNTS,
          Manifest.permission.GET_ACCOUNTS);
    }
  }

  /**
   * Called when an activity launched here (specifically, AccountPicker
   * and authorization) exits, giving you the requestCode you started it with,
   * the resultCode it returned, and any additional data from it.
   *
   * @param requestCode code indicating which activity result is incoming.
   * @param resultCode  code indicating the result of the incoming
   *                    activity result.
   * @param data        Intent (containing result data) returned by incoming
   *                    activity result.
   */
  @Override
  protected void onActivityResult(
      int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    switch (requestCode) {
      case REQUEST_GOOGLE_PLAY_SERVICES:
        if (resultCode != RESULT_OK) {
          textOutput.setText(
              getString(R.string.installGooglePlayMessage));
        } else {
          getResultsFromApi();
        }
        break;
      case REQUEST_ACCOUNT_PICKER:
        if (resultCode == RESULT_OK && data != null &&
            data.getExtras() != null) {
          String accountName =
              data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
          if (accountName != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(PREF_ACCOUNT_NAME, accountName).apply();
            credentials.setSelectedAccountName(accountName);
            getResultsFromApi();
          }
        }
        break;
      case REQUEST_AUTHORIZATION:
        if (resultCode == RESULT_OK) {
          getResultsFromApi();
        }
        break;
    }
  }

  /**
   * Respond to requests for permissions at runtime for API 23 and above.
   *
   * @param requestCode  The request code passed in
   *                     requestPermissions(android.app.Activity, String, int, String[])
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions
   *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    EasyPermissions.onRequestPermissionsResult(
        requestCode, permissions, grantResults, this);
  }

  /**
   * Callback for when a permission is granted using the EasyPermissions
   * library.
   *
   * @param requestCode The request code associated with the requested
   *                    permission
   * @param list        The requested permission list. Never null.
   */
  @Override
  public void onPermissionsGranted(int requestCode, List<String> list) {
    // Do nothing.
  }

  /**
   * Callback for when a permission is denied using the EasyPermissions
   * library.
   *
   * @param requestCode The request code associated with the requested
   *                    permission
   * @param list        The requested permission list. Never null.
   */
  @Override
  public void onPermissionsDenied(int requestCode, List<String> list) {
    // Do nothing.
  }

  /**
   * Checks whether the device currently has a network connection.
   *
   * @return true if the device has a network connection, false otherwise.
   */
  private boolean isDeviceOnline() {
    ConnectivityManager connMgr =
        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    assert connMgr != null;
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }

  /**
   * Check that Google Play services APK is installed and up to date.
   *
   * @return true if Google Play Services is available and up to
   * date on this device; false otherwise.
   */
  private boolean isGooglePlayServicesAvailable() {
    GoogleApiAvailability apiAvailability =
        GoogleApiAvailability.getInstance();
    final int connectionStatusCode =
        apiAvailability.isGooglePlayServicesAvailable(this);
    return connectionStatusCode == ConnectionResult.SUCCESS;
  }

  /**
   * Attempt to resolve a missing, out-of-date, invalid or disabled Google
   * Play Services installation via a user dialog, if possible.
   */
  private void acquireGooglePlayServices() {
    GoogleApiAvailability apiAvailability =
        GoogleApiAvailability.getInstance();
    final int connectionStatusCode =
        apiAvailability.isGooglePlayServicesAvailable(this);
    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
    }
  }


  /**
   * Display an error dialog showing that Google Play Services is missing
   * or out of date.
   *
   * @param connectionStatusCode code describing the presence (or lack of)
   *                             Google Play Services on this device.
   */
  void showGooglePlayServicesAvailabilityErrorDialog(
      final int connectionStatusCode) {
    GoogleApiAvailability apiAvailability =
        GoogleApiAvailability.getInstance();
    Dialog dialog = apiAvailability.getErrorDialog(
        GoogleSheetAPI.this,
        connectionStatusCode,
        REQUEST_GOOGLE_PLAY_SERVICES);
    dialog.show();
  }

}