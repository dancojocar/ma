import android.util.Log

fun logd(message: String? = "missing message", cause: Throwable? = null) {
    Log.d("MovieApp", message, cause)
}

fun loge(message: String? = "missing message", cause: Throwable? = null) {
    Log.e("MovieApp", message, cause)
}
