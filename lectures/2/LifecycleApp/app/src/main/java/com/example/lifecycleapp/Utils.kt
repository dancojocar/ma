package com.example.lifecycleapp

import android.util.Log

const val TAG = "main"

fun Any.logd(message: String, cause: Throwable? = null) {
  Log.d(TAG, message, cause)
}