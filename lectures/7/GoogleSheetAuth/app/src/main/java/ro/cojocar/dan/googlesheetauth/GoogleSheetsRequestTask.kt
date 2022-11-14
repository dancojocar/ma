package ro.cojocar.dan.googlesheetauth

import android.util.Log
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import java.io.IOException

/**
 * An asynchronous task that handles the Google Sheets API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
class GoogleSheetsRequestTask(credentials: GoogleAccountCredential) {
  private val service: Sheets

  init {
    val transport = AndroidHttp.newCompatibleTransport()
    val jsonFactory = GsonFactory.getDefaultInstance()
    service = Sheets.Builder(
        transport, jsonFactory, credentials
    )
        .setApplicationName("GoogleSheetsRequestTask")
        .build()
  }

  /**
   * Fetch a list of names and majors of students in a sample spreadsheet:
   * https://docs.google.com/spreadsheets/d/1d2cGX1jJhjzWQrU8UONUzg6Gd7R6LNqEHfcPUzb5L1I/edit
   *
   * @return List of names and majors
   * @throws IOException when trying to access the document
   */
  val dataFromApi: String
    get() {
      val spreadsheetId = "1d2cGX1jJhjzWQrU8UONUzg6Gd7R6LNqEHfcPUzb5L1I"
      val range = "Class Data!A2:E20"
      val results = StringBuilder()
      try {
        val response = service.spreadsheets().values()
            .get(spreadsheetId, range)
            .execute()
        val values = response.getValues()
        if (values != null) {
          results.append("Name, Major\n")
          for (row in values) {
            results.append("${row[0]}, ${row[4]}\n")
          }
        }
      } catch (e: UserRecoverableAuthIOException) {
        throw e
      } catch (e: Exception) {
        val errorMessage = "Received an error while trying to connect: $e"
        results.append(errorMessage)
        Log.e(this.javaClass.simpleName, errorMessage, e)
      }
      return results.toString()
    }
}
