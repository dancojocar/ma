package com.android.example.filelocker

import android.util.Log

const val LOG_TAG = "Main"
fun Any.loge(message: String, cause: Throwable? = null) {
    Log.e(LOG_TAG, message, cause)
}

fun Any.logd(message: String, cause: Throwable? = null) {
    Log.d(LOG_TAG, message, cause)
}

fun Any.logi(message: String, cause: Throwable? = null) {
    Log.i(LOG_TAG, message, cause)
}

fun Any.logw(message: String, cause: Throwable? = null) {
    Log.w(LOG_TAG, message, cause)
}