package ro.cojocar.dan.rxlifecycle

import android.content.Context
import android.util.Log
import android.widget.Toast

val TAG = "Main"

fun Any.logi(message: String = "Empty message!") {
  Log.i(TAG, message)
}

fun Any.logd(message: String = "Empty message!", cause: Throwable? = null) {
  Log.d(TAG, message, cause)
}

fun Any.loge(message: String = "Empty message!", error: Throwable) {
  Log.e(TAG, message, error)
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, message, duration).show()
}