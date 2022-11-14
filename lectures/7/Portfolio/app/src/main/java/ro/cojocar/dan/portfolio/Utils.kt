package ro.cojocar.dan.portfolio

import android.content.Context
import android.util.Log
import android.util.SparseArray
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import ro.cojocar.dan.portfolio.domain.Portfolio

private const val CACHE_TIME = 1000L * 60L * 60L // one hour

fun Any.logd(message: Any? = "no message!") {
  Log.d(this.javaClass.simpleName, message.toString())
}

fun SparseArray<List<Portfolio>>.getFreshPortfolios(userId: Int): List<Portfolio>? {
  return get(userId)?.takeIf {
    logd("Fetch from cache")
    it.isNotEmpty() && it.first().lastModified > currentTimeMs - CACHE_TIME
  }
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): android.view.View {
  val inflater = android.view.LayoutInflater.from(context)
  return inflater.inflate(layoutId, this, attachToRoot)
}

fun Context.toast(message: CharSequence) {
  Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

val currentTimeMs get() = System.currentTimeMillis()