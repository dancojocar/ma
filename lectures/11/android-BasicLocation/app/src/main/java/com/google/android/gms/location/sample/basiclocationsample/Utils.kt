package com.google.android.gms.location.sample.basiclocationsample

import android.util.Log

fun Any.logi(message: String = "no message!") {
  Log.i("Location", message)
}


fun Any.logd(message: String = "no message!", cause: Throwable? = null) {
  Log.d("Location", message, cause)
}

fun Any.logw(message: String = "no message!", cause: Throwable? = null) {
  Log.w("Location", message, cause)
}

fun Any.loge(message: String = "no message!", cause: Throwable? = null) {
  Log.e("Location", message, cause)
}