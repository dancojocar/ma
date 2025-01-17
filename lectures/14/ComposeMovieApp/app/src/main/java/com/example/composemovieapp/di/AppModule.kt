package com.example.composemovieapp.di

import com.example.composemovieapp.movies.repo.MoviesRepository
import com.example.composemovieapp.movies.repo.MoviesRepositoryImpl
import com.example.composemovieapp.movies.service.MoviesService
import com.example.composemovieapp.movies.usecase.AddMovieUseCase
import com.example.composemovieapp.movies.usecase.AddMovieUseCaseImpl
import com.example.composemovieapp.movies.usecase.GetMoviesUseCase
import com.example.composemovieapp.movies.usecase.GetMoviesUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesMoviesService(retrofit: Retrofit): MoviesService {
        return retrofit.create(MoviesService::class.java)
    }

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Module
    @InstallIn(SingletonComponent::class)
    interface AppModuleInt {

        @Binds
        @Singleton
        fun provideMoviesRepository(repo: MoviesRepositoryImpl): MoviesRepository

        @Binds
        @Singleton
        fun provideGetMoviesUseCase(uc: GetMoviesUseCaseImpl): GetMoviesUseCase

        @Binds
        @Singleton
        fun provideAddMovieUseCase(uc: AddMovieUseCaseImpl): AddMovieUseCase
    }

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class IoDispatcher

}