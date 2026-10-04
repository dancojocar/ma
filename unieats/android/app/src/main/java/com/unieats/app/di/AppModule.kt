package com.unieats.app.di

import android.content.Context
import androidx.room.Room
import com.unieats.app.BuildConfig
import com.unieats.app.data.auth.AuthTokens
import com.unieats.app.data.local.UniEatsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LiveUrl

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @ApiBaseUrl
    fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @LiveUrl
    fun provideLiveUrl(): String = BuildConfig.LIVE_URL

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): UniEatsDatabase =
        Room.databaseBuilder(context, UniEatsDatabase::class.java, "unieats.db").build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideHttpClient(
        json: Json,
        @ApiBaseUrl baseUrl: String,
        tokens: AuthTokens
    ): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true
        HttpResponseValidator {
            // A 401 on a request that carried a token means the session is over: dropping the
            // token sends the UI back to the login screen.
            handleResponseExceptionWithRequest { cause, request ->
                if (cause is ClientRequestException &&
                    cause.response.status == HttpStatusCode.Unauthorized &&
                    request.headers[HttpHeaders.Authorization] != null
                ) {
                    tokens.clear()
                }
            }
        }
        defaultRequest { url(baseUrl) }
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 15_000
        }
        install(HttpRequestRetry) {
            // 1 attempt + 2 retries = 3 tries, waiting ~1 s then ~2 s.
            retryOnServerErrors(maxRetries = 2)
            retryOnException(maxRetries = 2, retryOnTimeout = true)
            exponentialDelay(baseDelayMs = 500)
        }
        install(WebSockets) {
            pingIntervalMillis = 20_000
        }
    }
}
