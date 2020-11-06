package com.codelab.android.datastore

import android.util.Log

fun Any.loge(message: String, cause: Throwable? = null) {
    Log.e("Main", message, cause)
}