package ro.ubbcluj.cs.books


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

internal class Manager {

  fun networkConnectivity(context: Context): Boolean {
    var result = false
    val cm = context
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.run {
      cm.getNetworkCapabilities(cm.activeNetwork)?.run {
        result = when {
          hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
          hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
          hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
          else -> false
        }
      }
    }
    return result
  }
}
