package ro.cojocar.dan.fileprovider

import android.util.Log

fun Any.logd(message: Any? = "no message!") {
  Log.d(this.javaClass.simpleName, message.toString())
}

fun Any.loge(message: Any? = "no message!", error: Throwable?) {
  Log.e(this.javaClass.simpleName, message.toString(), error)
}