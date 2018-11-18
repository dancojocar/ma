package ro.cojocar.dan.coroutinedemo

import android.util.Log

fun Any.logd(message: Any? = "Empty message!") {
    Log.d(this.javaClass.simpleName, message.toString())
}