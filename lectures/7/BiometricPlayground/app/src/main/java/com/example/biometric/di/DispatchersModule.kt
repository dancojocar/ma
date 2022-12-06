package com.example.biometric.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Singleton
    @Provides
    @IoDispatcher
    fun ioDispatcher() = Dispatchers.IO

    @Singleton
    @Provides
    @DefaultDispatcher
    fun defaultDispatcher() = Dispatchers.Default
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher