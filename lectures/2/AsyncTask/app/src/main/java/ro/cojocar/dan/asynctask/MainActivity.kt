package ro.cojocar.dan.asynctask

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ro.cojocar.dan.asynctask.databinding.ActivityMainBinding
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
  lateinit var binding: ActivityMainBinding

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.submitButton.setOnClickListener {
      hideSoftKeyboard(binding.inputText)
      showProgressIndicator()
      val urlString = binding.inputText.text.toString()
      if (urlString.trim().isNotEmpty() && isNetworkAvailable(this)) {
        DownloadWebPageTask(this).execute(urlString)
//                AnkoAsynkTaskAlternative(this).downloadData(urlString)
      } else {
        Toast.makeText(
          this, "No internet connection or empty URL!",
          Toast.LENGTH_LONG
        ).show()
        hideProgressIndicator()
      }
    }
  }

  private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.apply {
      return getNetworkCapabilities(activeNetwork)?.run {
        when {
          hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
          hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
          hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
          else -> false
        }
      } ?: false
    }
  }

  private fun hideSoftKeyboard(view: View) {
    val inputMethodManager =
      getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
  }

  fun hideProgressIndicator() {
    binding.outputText.visibility = View.VISIBLE
    binding.progressIndicator.visibility = View.GONE

  }

  fun showProgressIndicator() {
    binding.outputText.visibility = View.GONE
    binding.progressIndicator.visibility = View.VISIBLE
  }


  @Throws(IOException::class)
  fun downloadUrl(myUrl: String): String {
    var inputStream: InputStream? = null
    // Only display the first 500 characters of the retrieved
    // web page content.
    val len = 500

    try {
//            Thread.sleep(2000)
      val url = URL(myUrl)
      val conn = url.openConnection() as HttpURLConnection
      conn.readTimeout = 10000
      conn.connectTimeout = 15000
      conn.requestMethod = "GET"
      conn.doInput = true
      // Starts the query
      conn.connect()
      val response = conn.responseCode
      logd("The response is: $response")
      inputStream = conn.inputStream

      // Convert the InputStream into a string
      return readIt(inputStream!!, len)

      // Makes sure that the InputStream is closed after the app is
      // finished using it.
    } finally {
      inputStream?.close()
    }
  }

  @Throws(IOException::class, UnsupportedEncodingException::class)
  internal fun readIt(stream: InputStream, len: Int): String {
    val reader = InputStreamReader(stream, "UTF-8")
    val buffer = CharArray(len)
    reader.read(buffer, 0, len)
    return String(buffer)
  }


}

