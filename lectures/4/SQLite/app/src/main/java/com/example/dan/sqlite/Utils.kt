package com.example.dan.sqlite

import android.util.Log

fun Any.logd(message: Any? = "no message!") {
  Log.d("Main", message.toString())
}