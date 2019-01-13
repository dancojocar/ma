package ro.ubbcluj.cs.books.service


import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ServiceFactory {

  /**
   * Creates a retrofit service from an arbitrary class (clazz)
   * @param clazz Java interface of the retrofit service
   * @param endPoint REST endpoint url
   * @return retrofit service with defined endpoint
   */
  fun <T> createRetrofitService(clazz: Class<T>, endPoint: String): T {
    val retrofit = Retrofit.Builder()
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(endPoint)
        .build()

    return retrofit.create(clazz)
  }
}