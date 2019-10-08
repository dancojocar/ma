package ro.cojocar.dan.fragments

import android.util.Log

fun Any.logd(message: Any? = "No message!") {
  Log.d(this.javaClass.simpleName, message.toString())
}