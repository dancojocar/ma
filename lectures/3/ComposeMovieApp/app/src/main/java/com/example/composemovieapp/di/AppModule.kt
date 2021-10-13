package com.example.composemovieapp.di

import com.example.composemovieapp.movies.repo.MoviesRepository
import com.example.composemovieapp.movies.repo.MoviesRepositoryImpl
import com.example.composemovieapp.movies.service.MoviesService
import com.example.composemovieapp.movies.usecase.GetMoviesUseCase
import com.example.composemovieapp.movies.usecase.GetMoviesUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.cs.ubbcluj.ro/~dan/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providesMoviesService(retrofit: Retrofit): MoviesService {
        return retrofit.create(MoviesService::class.java)
    }


    @Module
    @InstallIn(SingletonComponent::class)
    interface AppModuleInt {

        @Binds
        @Singleton
        fun provideMoviesRepository(repo: MoviesRepositoryImpl): MoviesRepository

        @Binds
        @Singleton
        fun provideGetMoviesUseCase(uc: GetMoviesUseCaseImpl): GetMoviesUseCase

    }

}