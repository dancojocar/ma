package com.example.android.testing.unittesting.basicsample

import android.util.Log

fun Any.logi(message: String = "missing message", cause: Throwable? = null) {
  Log.i("Main", message, cause);
}

fun Any.logw(message: String = "missing message", cause: Throwable? = null) {
  Log.w("Main", message, cause);
}

fun Any.logd(message: String = "missing message", cause: Throwable? = null) {
  Log.d("Main", message, cause);
}

fun Any.loge(message: String = "missing message", cause: Throwable? = null) {
  Log.e("Main", message, cause);
}