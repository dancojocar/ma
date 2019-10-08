package ro.cojocar.dan.asynctask

import android.util.Log

fun Any.logd(message: Any? = "unused message!") {
    Log.d(this.javaClass.simpleName, message.toString())
}