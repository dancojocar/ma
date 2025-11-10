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
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import ro.cojocar.dan.googlesheetauth.ui.theme.GoogleSheetsAuthTheme

/**
 * Enable the api using:
 * https://console.developers.google.com/start/api?id=sheets.googleapis.com
 * <p>
 * Following: https://developers.google.com/drive/api/quickstart/java
 *
 * <p>
 * keytool -exportcert -keystore ~/.android/debug.keystore -list -v
 */
class MainActivity : ComponentActivity() {
  private lateinit var credentials: GoogleAccountCredential
  private val scopes = arrayOf(SheetsScopes.SPREADSHEETS_READONLY)
  
  private lateinit var accountPickerLauncher: ActivityResultLauncher<Intent>
  private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>
  private lateinit var playServicesLauncher: ActivityResultLauncher<Intent>
  private lateinit var permissionLauncher: ActivityResultLauncher<String>
  
  private val outputTextState = mutableStateOf("")
  private val isLoadingState = mutableStateOf(false)
  private val isButtonEnabledState = mutableStateOf(true)
  
  private var outputText: String
    get() = outputTextState.value
    set(value) { outputTextState.value = value }
  
  private var isLoading: Boolean
    get() = isLoadingState.value
    set(value) { isLoadingState.value = value }
  
  private var isButtonEnabled: Boolean
    get() = isButtonEnabledState.value
    set(value) { isButtonEnabledState.value = value }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    credentials = GoogleAccountCredential.usingOAuth2(this, scopes.toList())
      .setBackOff(ExponentialBackOff())
    
    // Register activity result launchers
    accountPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK && result.data != null) {
        val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
        if (accountName != null) {
          val prefs = PreferenceManager.getDefaultSharedPreferences(this)
          prefs.edit().putString(PREF_ACCOUNT_NAME, accountName).apply()
          credentials.selectedAccountName = accountName
          getResultsFromApi()
        }
      }
    }
    
    authorizationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode == Activity.RESULT_OK) {
        getResultsFromApi()
      }
    }
    
    playServicesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      if (result.resultCode != Activity.RESULT_OK) {
        outputText = getString(R.string.installGooglePlay)
      } else {
        getResultsFromApi()
      }
    }
    
    permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (isGranted) {
        chooseAccount()
      } else {
        outputText = getString(R.string.permissionDenied)
      }
    }
    
    setContent {
      GoogleSheetsAuthTheme {
        GoogleSheetsAuthScreen()
      }
    }
  }
  
  @Composable
  fun GoogleSheetsAuthScreen() {
    val outputTextValue by outputTextState
    val isLoadingValue by isLoadingState
    val isButtonEnabledValue by isButtonEnabledState
    
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
          onClick = {
            isButtonEnabled = false
            outputText = ""
            getResultsFromApi()
          },
          enabled = isButtonEnabledValue,
          modifier = Modifier.padding(top = 16.dp)
        ) {
          Text("Connect")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoadingValue) {
          CircularProgressIndicator()
        }
        
        Text(
          text = outputTextValue,
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        )
      }
    }
  }


  /**
   * Attempt to call the API, after verifying that all the preconditions are
   * satisfied. The preconditions are: Google Play Services installed, an
   * account was selected and the device currently has online access. If any
   * of the preconditions are not satisfied, the app will prompt the user as
   * appropriate.
   */
  private fun getResultsFromApi() {
    Log.d(TAG, "getResultsFromApi called")
    Log.d(TAG, "Current account: ${credentials.selectedAccountName}")
    
    if (!isGooglePlayServicesAvailable()) {
      Log.d(TAG, "Google Play Services not available")
      acquireGooglePlayServices()
      return
    }
    
    if (credentials.selectedAccountName == null) {
      Log.d(TAG, "No account selected, calling chooseAccount")
      chooseAccount()
      // Check if account was set synchronously from preferences
      if (credentials.selectedAccountName == null) {
        Log.d(TAG, "Account not set, waiting for user action")
        return
      }
      Log.d(TAG, "Account was set from preferences, continuing...")
    }
    
    if (!isDeviceOnline(this)) {
      Log.d(TAG, "Device is offline")
      outputText = getString(R.string.noConnection)
      isButtonEnabled = true
      return
    }
    
    Log.d(TAG, "All checks passed, making API call")
    isLoading = true
    lifecycleScope.launch(Dispatchers.IO) {
      try {
        val values = GoogleSheetsRequestTask(credentials).dataFromApi()
        launch(Dispatchers.Main) {
          isLoading = false
          outputText = values
          isButtonEnabled = true
        }
      } catch (e: UserRecoverableAuthIOException) {
        launch(Dispatchers.Main) {
          isLoading = false
          authorizationLauncher.launch(e.intent)
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
   * present.
   */
  private fun chooseAccount() {
    Log.d(TAG, "chooseAccount called")
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
      val prefs = PreferenceManager.getDefaultSharedPreferences(this)
      val accountName = prefs.getString(PREF_ACCOUNT_NAME, null)
      Log.d(TAG, "Saved account name: $accountName")
      if (accountName != null) {
        Log.d(TAG, "Attempting to set account: $accountName")
        try {
          credentials.selectedAccountName = accountName
          Log.d(TAG, "Credentials object after setting: ${credentials.selectedAccountName}")
          
          // The account might not exist on the device anymore, so clear preferences and show picker
          if (credentials.selectedAccountName == null) {
            Log.w(TAG, "Account was not accepted by credentials, clearing preferences and showing picker")
            prefs.edit().remove(PREF_ACCOUNT_NAME).apply()
            accountPickerLauncher.launch(credentials.newChooseAccountIntent())
          }
        } catch (e: Exception) {
          Log.e(TAG, "Error setting account: ${e.message}", e)
          accountPickerLauncher.launch(credentials.newChooseAccountIntent())
        }
      } else {
        Log.d(TAG, "No saved account, launching account picker")
        accountPickerLauncher.launch(credentials.newChooseAccountIntent())
      }
    } else {
      Log.d(TAG, "Permission not granted, requesting permission")
      permissionLauncher.launch(Manifest.permission.GET_ACCOUNTS)
    }
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
    apiAvailability.showErrorDialogFragment(this, connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES) {
      outputText = getString(R.string.installGooglePlay)
    }
  }

  companion object {
    private const val TAG = "MainActivity"
    const val REQUEST_ACCOUNT_PICKER = 1000
    const val REQUEST_AUTHORIZATION = 1001
    const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
    const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
    const val PREF_ACCOUNT_NAME = "accountName"
  }
}
