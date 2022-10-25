package ro.cojocar.dan.asynctask

import android.util.Log

fun Any.logd(message: String = "unused message!") {
    Log.d(this.javaClass.simpleName, message)
}