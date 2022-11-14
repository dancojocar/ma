package com.example.android.jobscheduler

import android.app.Activity
import android.util.Log
import android.widget.Toast

fun Any.logd(message: Any? = "no message!") {
  Log.d(this.javaClass.simpleName, message.toString())
}

fun Any.loge(message: Any? = "no message!", e: Throwable?) {
  Log.e(this.javaClass.simpleName, message.toString(), e)
}

fun Activity.showToast(text: String) {
  Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}