package ro.cojocar.dan.coroutinedemo

import android.util.Log

fun Any.logd(message: Any? = "Empty message!", cause: Throwable? = null) {
  Log.d("Main", message.toString(), cause)
}

fun Any.loge(message: Any? = "Empty message!", cause: Throwable? = null) {
  Log.e("Main", message.toString(), cause)
}
