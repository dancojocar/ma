package com.example.android.common

import android.content.Context
import android.util.Log
import android.widget.Toast

fun Any.logd(message: Any? = "no message!") {
  Log.d("MainActivity", message.toString())
}

fun Any.toast(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT): Toast {
  return Toast.makeText(context, message, duration).apply { show() }
}