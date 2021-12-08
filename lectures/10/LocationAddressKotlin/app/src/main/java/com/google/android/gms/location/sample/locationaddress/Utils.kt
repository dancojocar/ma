package com.google.android.gms.location.sample.locationaddress

import android.util.Log

const val TAG = "Main"

fun Any.logd(message: String = "missing message!", cause: Throwable? = null) {
  Log.d(TAG, message, cause)
}

fun Any.logi(message: String = "missing message!", cause: Throwable? = null) {
  Log.i(TAG, message, cause)
}

fun Any.logw(message: String = "missing message!", cause: Throwable? = null) {
  Log.w(TAG, message, cause)
}

fun Any.loge(message: String = "missing message!", cause: Throwable? = null) {
  Log.e(TAG, message, cause)
}
