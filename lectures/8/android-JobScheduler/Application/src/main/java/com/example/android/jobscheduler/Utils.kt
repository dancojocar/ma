package com.example.android.jobscheduler

import android.util.Log

fun Any.logi(message: String) {
  Log.i(this.javaClass.simpleName, message)
}


fun Any.logd(message: String? = "no message", cause: Throwable? = null) {
  Log.d(this.javaClass.simpleName, message, cause)
}

fun Any.loge(message: String? = "no message", cause: Throwable? = null) {
  Log.e(this.javaClass.simpleName, message, cause)
}