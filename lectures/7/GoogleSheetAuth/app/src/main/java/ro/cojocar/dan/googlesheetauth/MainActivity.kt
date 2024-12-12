package ro.cojocar.dan.googlesheetauth

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.sheets.v4.SheetsScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.cojocar.dan.googlesheetauth.databinding.ActivityMainBinding

/**
 * Enable the api using:
 * https://console.developers.google.com/start/api?id=sheets.googleapis.com
 * <p>
 * Following: https://developers.google.com/drive/api/quickstart/java
 *
 * <p>
 * keytool -exportcert -keystore ~/.android/debug.keystore -list -v
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var credentials: GoogleAccountCredential

  private val scopes = arrayOf(SheetsScopes.SPREADSHEETS_READONLY)

  /**
   * Create the main activity.
   *
   * @param savedInstanceState previously saved instance data.
   */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.button.setOnClickListener {
      binding.button.isEnabled = false
      binding.output.text = ""
      getResultsFromApi()
    }

    credentials = GoogleAccountCredential.usingOAuth2(this, scopes.toList())
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
      binding.output.text = getString(R.string.noConnection)
    } else {
      binding.progressBar.visibility = View.VISIBLE
      lifecycleScope.launch(Dispatchers.IO) {
        try {
          val values = GoogleSheetsRequestTask(credentials).dataFromApi()
          launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            binding.output.text = values
            binding.button.isEnabled = true
          }
        } catch (e: UserRecoverableAuthIOException) {
          startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
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
  private fun chooseAccount() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
      val prefs = PreferenceManager.getDefaultSharedPreferences(this)
      val accountName = prefs.getString(PREF_ACCOUNT_NAME, null)
      if (accountName != null) {
        credentials.selectedAccountName = accountName
        getResultsFromApi()
      } else {
        startActivityForResult(credentials.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER)
      }
    } else {
      ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.GET_ACCOUNTS),
        REQUEST_PERMISSION_GET_ACCOUNTS
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
        binding.output.text = getString(R.string.installGooglePlay)
      } else {
        getResultsFromApi()
      }
      REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null) {
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

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    if (requestCode == REQUEST_PERMISSION_GET_ACCOUNTS) {
      if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        chooseAccount()
      } else {
        binding.output.text = getString(R.string.permissionDenied)
      }
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  private fun isDeviceOnline(context: Context): Boolean {
    val connectivityManager =
      context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
      val network = connectivityManager.activeNetwork
      val capabilities = connectivityManager.getNetworkCapabilities(network)
      return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
          || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }
    return false
  }

  private fun isGooglePlayServicesAvailable(): Boolean {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
    return connectionStatusCode == ConnectionResult.SUCCESS
  }

  private fun acquireGooglePlayServices() {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
    if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
      showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
    }
  }

  private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
    val apiAvailability = GoogleApiAvailability.getInstance()
    val dialog = apiAvailability.getErrorDialog(this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES)
    dialog?.show()
  }

  companion object {
    const val REQUEST_ACCOUNT_PICKER = 1000
    const val REQUEST_AUTHORIZATION = 1001
    const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    const val PREF_ACCOUNT_NAME = "accountName"
  }
}
