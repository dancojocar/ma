package ro.cojocar.dan.portfolio.service

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor constructor() : Interceptor {
  var token: String? = null

  override fun intercept(chain: Interceptor.Chain): Response {

    val original = chain.request()

    val originalUrl = original.url()
    val encodedPath = originalUrl.encodedPath()
    if (token == null || (original.method() == "post" && encodedPath.contains("/token-auth"))) {
      return chain.proceed(original)
    }

    val requestBuilder = original.newBuilder()
      .addHeader("Authorization", token!!)
      .url(originalUrl)

    val request = requestBuilder.build()
    return chain.proceed(request)
  }
}