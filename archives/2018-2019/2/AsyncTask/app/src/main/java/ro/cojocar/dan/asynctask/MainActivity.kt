package ro.cojocar.dan.asynctask

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        submitButton.setOnClickListener {
            hideSoftKeyboard(inputText)
            showProgressIndicator()
            val urlString = inputText.text.toString()
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            if (urlString.trim().isNotEmpty() && networkInfo != null && networkInfo.isConnected) {
                DownloadWebPageTask().execute(urlString)
//                AnkoAsynkTaskAlternative().downloadData(urlString)
            } else {
                Toast.makeText(this, "No internet connection or empty URL!", Toast.LENGTH_LONG).show()
                hideProgressIndicator()
            }
        }
    }

    fun hideSoftKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    inner class DownloadWebPageTask : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            showProgressIndicator()
        }

        override fun doInBackground(vararg urls: String): String {
            // params comes from the execute() call: params[0] is the url.
            return try {
                downloadUrl(urls[0])
            } catch (e: IOException) {
                "Unable to retrieve web page. URL may be invalid."
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        override fun onPostExecute(result: String) {
            outputText.text = result
            hideProgressIndicator()
        }

    }

    private fun hideProgressIndicator() {
        outputText.visibility = View.VISIBLE
        progressIndicator.visibility = View.GONE

    }

    private fun showProgressIndicator() {
        outputText.visibility = View.GONE
        progressIndicator.visibility = View.VISIBLE
    }


    @Throws(IOException::class)
    private fun downloadUrl(myUrl: String): String {
        var inputStream: InputStream? = null
        // Only display the first 500 characters of the retrieved
        // web page content.
        val len = 500

        try {
//                Thread.sleep(2000)
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
            inputStream = conn.getInputStream()

            // Convert the InputStream into a string
            return readIt(inputStream, len)

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (inputStream != null) {
                inputStream.close()
            }
        }
    }

    @Throws(IOException::class, UnsupportedEncodingException::class)
    internal fun readIt(stream: InputStream?, len: Int): String {
        val reader = InputStreamReader(stream, "UTF-8")
        val buffer = CharArray(len)
        reader.read(buffer, 0, len)
        return String(buffer)
    }

    inner class AnkoAsynkTaskAlternative {
        fun downloadData(url: String) {
            showProgressIndicator()
            doAsync {
                //Execute all the long running tasks here
                val output: String = downloadUrl(url)
                uiThread {
                    outputText.text = output
                    hideProgressIndicator()
                    alert("Downloaded data successfully", "Hi I'm an alert") {
                        yesButton { toast("Yay !") }
                        noButton { toast(":( !") }
                    }.show()
                }
            }
        }
    }

}
