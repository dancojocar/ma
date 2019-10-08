package ro.cojocar.dan.myapplication

import android.util.Log

fun Any.logd(message: Any? = "No message provided!") {
    Log.d(this.javaClass.simpleName, message.toString())
}