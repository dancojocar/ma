package com.example.android.largeimages

import android.util.Log

fun Any.loge(message: String = "No message!", cause: Throwable? = null) {
  Log.e(this.javaClass.simpleName, message, cause)
}