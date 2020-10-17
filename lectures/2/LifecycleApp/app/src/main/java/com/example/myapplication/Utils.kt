package com.example.myapplication

import android.util.Log

fun Any.logd(message: String, cause: Throwable?=null){
    Log.d("Main", message, cause)
}