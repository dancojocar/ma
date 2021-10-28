import android.util.Log

fun Any.logd(message: String = "missing message", cause: Throwable? = null) {
  Log.d("Main", message, cause)
}

fun Any.loge(message: String = "missing message", cause: Throwable? = null) {
  Log.e("Main", message, cause)
}
