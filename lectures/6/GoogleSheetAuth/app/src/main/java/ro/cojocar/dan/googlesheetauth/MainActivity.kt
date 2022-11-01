package ro.cojocar.dan.googlesheetauth

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

/**
 * Enable the api using:
 * https://console.developers.google.com/start/api?id=sheets.googleapis.com
 * <p>
 * Following: https://developers.google.com/drive/android/get-started
 *
 * <p>
 * keytool -exportcert -keystore ~/.android/debug.keystore -list -v
 */
class MainActivity : AppCompatActivity() {
  private lateinit var credentials: GoogleAccountCredential

  private val scopes = arrayOf(SheetsScopes.SPREADSHEETS_READONLY)

  /**
   * Create the main activity.
   *
   * @param savedInstanceState previously saved instance data.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    button.setOnClickListener {
      button.isEnabled = false
      output.text = ""
      getResultsFromApi()
    }

    // Initialize credentials and service object.
    credentials = GoogleAccountCredential.usingOAuth2(this, listOf(*scopes))
        .setBackOff(ExponentialBackOff())
  }


  /**
   * Attempt to call the API, after verifying that all the preconditions are
   * satisfied. The preconditions are: Google Play Services installed, an
   * account was selected and the device currently has online access. If any
   * of the preconditions are not satisfied, the app will prompt the user as
   * appropriate.
   */
  private fun getResultsFromApi() {
    if (!isGooglePlayServicesAvailable()) {
      acquireGooglePlayServices()
    } else if (credentials.selectedAccountName == null) {
      chooseAccount()
    } else if (!isDeviceOnline(this)) {
      output.text = getString(R.string.noConnection)
    } else {
      progressBar.visibility = View.VISIBLE
      doAsync {

        try {
          val values = GoogleSheetsRequestTask(credentials).dataFromApi
          uiThread {
            progressBar.visibility = View.GONE
            output.text = values
            button.isEnabled = true
          }
        } catch (e: UserRecoverableAuthIOException) {
          logi("Trying again")
          // Start a dialog from which the user can choose an account
          startActivityForResult(e.intent, REQUEST_GOOGLE_PLAY_SERVICES)
        }
      }
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
  private fun chooseAccount() {
    if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
      val prefs = PreferenceManager.getDefaultSharedPreferences(this)
      val accountName = prefs.getString(PREF_ACCOUNT_NAME, null)
      if (accountName != null) {
        credentials.selectedAccountName = accountName
        getResultsFromApi()
      } else {
        // Start a dialog from which the user can choose an account
        startActivityForResult(credentials.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
      }
    } else {
      // Request the GET_ACCOUNTS permission via a user dialog
      EasyPermissions.requestPermissions(
          this,
          "This app needs to access your Google account (via Contacts).",
          REQUEST_PERMISSION_GET_ACCOUNTS,
          Manifest.permission.GET_ACCOUNTS
      )
    }
  }

  /**
   * Called when an activity launched here (specifically, AccountPicker
   * and authorization) exits, giving you the requestCode you started it with,
   * the resultCode it returned, and any additional data from it.
   *
   * @param requestCode code indicating which activity result is incoming.
   * @param resultCode  code indicating the result of the incoming
   * activity result.
   * @param data        Intent (containing result data) returned by incoming
   * activity result.
   */
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (requestCode) {
      REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
        output.text = getString(R.string.installGooglePlay)
      } else {
        getResultsFromApi()
      }
      REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
        val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (accountName != null) {
          val prefs = PreferenceManager.getDefaultSharedPreferences(this)
          prefs.edit().putString(PREF_ACCOUNT_NAME, accountName).apply()
          credentials.selectedAccountName = accountName
          getResultsFromApi()
        }
      }
      REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
        getResultsFromApi()
      }
    }
  }

  /**
   * Respond to requests for permissions at runtime for API 23 and above.
   *
   * @param requestCode  The request code passed in
   * requestPermissions(android.app.Activity, String, int, String[])
   * @param permissions  The requested permissions. Never null.
   * @param grantResults The grant results for the corresponding permissions
   * which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
   */
  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    EasyPermissions.onRequestPermissionsResult(
        requestCode, permissions, grantResults, this
    )
  }

  /**
   * Checks whether the device currently has a network connection.
   *
   * @return true if the device has a network connection, false otherwise.
   */
  private fun isDeviceOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val network = connectivityManager.activeNetwork
      val capabilities = connectivityManager.getNetworkCapabilities(network)
      return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
          || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
    return false
  }

  /**
   * Check that Google Play services APK is installed and up to date.
   *
   * @return true if Google Play Services is available and up to
   * date on this device; false otherwise.
   */
  private fun isGooglePlayServicesAvailable(): Boolean {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
    return connectionStatusCode == ConnectionResult.SUCCESS
  }

  /**
   * Attempt to resolve a missing, out-of-date, invalid or disabled Google
   * Play Services installation via a user dialog, if possible.
   */
  private fun acquireGooglePlayServices() {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
    }
  }

  /**
   * Display an error dialog showing that Google Play Services is missing
   * or out of date.
   *
   * @param connectionStatusCode code describing the presence (or lack of)
   * Google Play Services on this device.
   */
  private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val dialog = apiAvailability.getErrorDialog(
        this@MainActivity,
        connectionStatusCode,
        REQUEST_GOOGLE_PLAY_SERVICES
    )
    dialog.show()
  }

  companion object {
    const val REQUEST_ACCOUNT_PICKER = 1000
    const val REQUEST_AUTHORIZATION = 1001
    const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    const val PREF_ACCOUNT_NAME = "accountName"
  }
}
