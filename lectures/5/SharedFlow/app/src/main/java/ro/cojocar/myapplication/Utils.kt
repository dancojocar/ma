package ro.cojocar.myapplication

import android.util.Log

fun Any.logd(message: String, cause: Throwable? = null) {
  Log.d("main", message, cause)
}