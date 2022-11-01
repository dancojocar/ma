package ro.cojocar.dan.rxdemo

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Any.logd(message: Any? = "Empty message!", cause: Throwable? = null) {
  Log.d(this.javaClass.simpleName, message.toString(), cause)
}

fun Any.loge(message: Any? = "Empty message!", error: Throwable) {
  Log.e(this.javaClass.simpleName, message.toString(), error)
}

fun Context.toast(message: String, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, message, duration).show()
}