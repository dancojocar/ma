package ro.cojocar.dan.wsclient

import android.util.Log

fun Any.logd(message: Any? = "no message!", cause: Throwable? = null) {
    Log.d("WSS", message.toString(), cause)
}