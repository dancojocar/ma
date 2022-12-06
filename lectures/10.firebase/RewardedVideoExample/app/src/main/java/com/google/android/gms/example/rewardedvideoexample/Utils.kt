package com.google.android.gms.example.rewardedvideoexample

import android.util.Log

const val TAG = "Main"

fun Any.logd(message: String = "missing!", cause: Throwable? = null) {
  Log.d(TAG, message, cause)
}