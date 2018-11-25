package com.example.android.messagingservice

import android.util.Log

fun Any.logd(message: Any? = "no message!") {
  Log.d(this.javaClass.simpleName, message.toString())
}

fun Any.loge(message: Any? = "no message!", e: Throwable?) {
  Log.e(this.javaClass.simpleName, message.toString(), e)
}