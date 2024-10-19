package com.example.lazycolumndemo

import android.app.Activity
import android.util.Log

const val TAG = "main"

fun logd(message: String, cause: Throwable? = null) {
  Log.d(TAG, message, cause)
}