package ro.cojocar.dan.rxdemo

import android.util.Log

fun Any.logd(message: Any? = "Empty message!") {
    Log.d(this.javaClass.simpleName, message.toString())
}

fun Any.loge(message: Any? = "Empty message!", error: Throwable) {
    Log.e(this.javaClass.simpleName, message.toString(), error)
}