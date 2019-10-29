package ro.cojocar.dan.googlesheetauth

import android.util.Log

fun Any.logd(message: String = "No message!", cause: Throwable? = null) {
  Log.d(this.javaClass.simpleName, message, cause)
}

fun Any.logi(message: String = "No message!") {
  Log.i(this.javaClass.simpleName, message)
}