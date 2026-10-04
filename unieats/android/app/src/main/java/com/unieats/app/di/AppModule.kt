package com.unieats.app.di

import com.unieats.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @ApiBaseUrl
    fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json, @ApiBaseUrl baseUrl: String): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true
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
    }
}
