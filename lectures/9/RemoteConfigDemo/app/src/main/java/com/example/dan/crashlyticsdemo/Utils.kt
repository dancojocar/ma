package com.example.dan.crashlyticsdemo

import android.util.Log

const val TAG = "Main"

fun Any.logd(message: String = "missing message!", cause: Throwable? = null) {
  Log.d(TAG, message, cause)
}