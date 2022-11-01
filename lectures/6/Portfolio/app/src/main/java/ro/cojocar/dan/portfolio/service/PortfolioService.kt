package ro.cojocar.dan.portfolio.service

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ro.cojocar.dan.portfolio.domain.Portfolio

object PortfolioApi {
  //Running from the emulator, start the server first.
  private const val URL = "http://10.0.2.2:8080/"

  interface Service {
    @GET("/p")
    suspend fun getPortfolios(): List<Portfolio>

    @POST("/token-auth")
    suspend fun authenticate(@Body login: LoginCredentials): TokenHolder

  }

  private val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    this.level = HttpLoggingInterceptor.Level.BODY
  }

  val tokenInterceptor = TokenInterceptor()

  private val client: OkHttpClient = OkHttpClient.Builder().apply {
    this.addInterceptor(interceptor)
    this.addInterceptor(tokenInterceptor)
  }.build()


  private var gson = GsonBuilder()
    .setLenient()
    .create()

  private val retrofit = Retrofit.Builder()
    .baseUrl(URL)
    .addConverterFactory(GsonConverterFactory.create(gson))
    .client(client)
    .build()

  val service: Service = retrofit.create(Service::class.java)
}