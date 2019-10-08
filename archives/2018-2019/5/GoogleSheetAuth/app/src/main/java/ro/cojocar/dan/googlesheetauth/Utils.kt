package ro.cojocar.dan.googlesheetauth

import android.util.Log

fun Any.logd(message: Any? = "No message!") {
    Log.d(this.javaClass.simpleName, message.toString())
}

fun Any.logi(message: Any? = "No message!") {
    Log.i(this.javaClass.simpleName, message.toString())
}