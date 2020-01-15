package ro.ubbcluj.cs.books.utils

import android.util.Log

fun Any.logi(message: Any? = "no message!") {
  Log.i(this.javaClass.simpleName, message.toString())
}

fun Any.logd(message: Any? = "no message!", cause: Throwable? = null) {
  Log.d(this.javaClass.simpleName, message.toString(), cause)
}

fun Any.logw(message: Any? = "no message!", cause: Throwable? = null) {
  Log.w(this.javaClass.simpleName, message.toString(), cause)
}

fun Any.loge(message: Any? = "no message!", cause: Throwable? = null) {
  Log.e(this.javaClass.simpleName, message.toString(), cause)
}