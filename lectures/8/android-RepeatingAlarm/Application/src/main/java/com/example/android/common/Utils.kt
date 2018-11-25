package com.example.android.common

import android.util.Log

fun Any.logd(message: Any? = "no message!") {
  Log.d("ALARM", message.toString())
}