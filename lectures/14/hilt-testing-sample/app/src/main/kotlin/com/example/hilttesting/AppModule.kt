package com.example.hilttesting

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository for greeting messages.
 */
interface GreetingRepository {
  fun getGreeting(): String
  fun getPersonalizedGreeting(name: String): String
}

/**
 * Production implementation of GreetingRepository.
 */
class GreetingRepositoryImpl : GreetingRepository {
  override fun getGreeting(): String = "Hello from Hilt!"

  override fun getPersonalizedGreeting(name: String): String =
    "Hello, $name! Welcome to Hilt Testing."
}

/**
 * Hilt module providing the production dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideGreetingRepository(): GreetingRepository = GreetingRepositoryImpl()
}
