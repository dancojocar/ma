package ro.cojocar.dan.navigationdrawer

import android.util.Log

fun Any.logd(message: Any? = "not defined") {
    Log.d(this.javaClass.simpleName, message.toString())
}