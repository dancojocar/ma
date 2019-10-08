package ro.cojocar.dan.portfolio

import android.content.Context
import android.support.annotation.LayoutRes
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ro.cojocar.dan.portfolio.domain.Portfolio

private val CACHE_TIME = 1000L * 60L * 60L // one hour

fun Any.logd(message: Any? = "no message!") {
    Log.d(this.javaClass.simpleName, message.toString())
}

/**
 * @throws IllegalStateException
 */
suspend fun <T> Call<T>.await(): T = suspendCancellableCoroutine { continuation ->

    continuation.invokeOnCancellation { cancel() }

    val callback = object : Callback<T> {
        override fun onFailure(call: Call<T>, t: Throwable) {
            continuation.cancel(t)
        }

        override fun onResponse(call: Call<T>, response: Response<T>) {
            response.isSuccessful || throw IllegalStateException("Http error ${response.code()}")
            val body = response.body() ?: throw IllegalStateException("Response body is null")
            continuation.resume(body)
        }
    }
    enqueue(callback)
}

fun Map<Long, List<Portfolio>>.getFreshPortfolios(userId: Long) = get(userId)?.takeIf {
    it.isNotEmpty() && it.first().lastModified + CACHE_TIME > currentTimeMs
}


fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): android.view.View {
    val inflater = android.view.LayoutInflater.from(context)
    return inflater.inflate(layoutId, this, attachToRoot)
}

fun Context.toast(message: CharSequence) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

val currentTimeMs get() = System.currentTimeMillis()