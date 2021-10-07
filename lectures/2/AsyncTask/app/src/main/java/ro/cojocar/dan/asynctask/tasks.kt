package ro.cojocar.dan.asynctask

import android.os.AsyncTask
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import java.io.IOException
import java.lang.ref.WeakReference

@Suppress("unused")
class DownloadWebPageTask(context: MainActivity) : AsyncTask<String, Void, String>() {
    private val activityReference: WeakReference<MainActivity> = WeakReference(context)
    override fun onPreExecute() {
        super.onPreExecute()
        activityReference.get()?.showProgressIndicator()
    }

    override fun doInBackground(vararg urls: String): String {
        // params comes from the execute() call: params[0] is the url.
        return try {
            activityReference.get()?.downloadUrl(urls[0])!!
        } catch (e: IOException) {
            "Unable to retrieve web page. URL may be invalid. message: ${e.message}"
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    override fun onPostExecute(result: String) {
        activityReference.get()?.outputText?.text = result
        activityReference.get()?.hideProgressIndicator()
    }

}

@Suppress("unused")
class AnkoAsynkTaskAlternative(context: MainActivity) {
    private val activityReference: WeakReference<MainActivity> = WeakReference(context)

    fun downloadData(url: String) {
        val activity = activityReference.get()
        activity?.showProgressIndicator()
        doAsync {
            //Execute all the long running tasks here
            val output: String = activity?.downloadUrl(url)!!
            uiThread {
                activity.apply {
                    outputText?.text = output
                    hideProgressIndicator()
                    alert(
                        "Downloaded data successfully",
                        "Hi I'm an alert"
                    ) {
                        yesButton { toast("Yay !") }
                        noButton { toast(":( !") }
                    }.show()
                }
            }
        }
    }
}
