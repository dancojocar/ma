package com.google.firebase.quickstart.database.kotlin

import android.util.Log

fun Any.logd(message: Any? = "No message!", cause: Throwable? = null) {
  Log.d(this.javaClass.simpleName, message.toString(), cause)
}

fun Any.logw(message: Any? = "No message!", cause: Throwable? = null) {
  Log.w(this.javaClass.simpleName, message.toString(), cause)
}

fun Any.loge(message: Any? = "No message!", cause: Throwable? = null) {
  Log.e(this.javaClass.simpleName, message.toString(), cause)
}