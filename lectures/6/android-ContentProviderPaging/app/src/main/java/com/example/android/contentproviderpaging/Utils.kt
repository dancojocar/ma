package com.example.android.contentproviderpaging

import android.util.Log

fun Any.logd(message: String = "No message", cause: Throwable? = null) {
  Log.d(this.javaClass.simpleName, message, cause)
}