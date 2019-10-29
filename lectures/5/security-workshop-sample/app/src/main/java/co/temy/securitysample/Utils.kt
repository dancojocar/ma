package co.temy.securitysample

import android.util.Log

fun Any.logi(message: String) {
  if (BuildConfig.DEBUG) Log.i(this.javaClass.simpleName, message)
}

fun Any.logd(message: String, cause: Throwable? = null) {
  if (BuildConfig.DEBUG) Log.d(this.javaClass.simpleName, message, cause)
}