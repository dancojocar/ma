package com.example.hilttesting

import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demonstrates Hilt testing patterns.
 * 
 * Key concepts:
 * - @HiltAndroidTest: Enables Hilt injection in tests
 * - @UninstallModules: Remove production modules
 * - Test modules: Provide fake/mock implementations
 * - @BindValue: Quick way to replace bindings
 */
@HiltAndroidTest
@UninstallModules(AppModule::class)  // Remove production module
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [30])
class GreetingViewModelHiltTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    // Inject the ViewModel - Hilt will use our test module
    @Inject
    lateinit var repository: GreetingRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `test module provides fake repository`() {
        // The injected repository should be our fake
        assertThat(repository.getGreeting()).isEqualTo("Test Greeting!")
    }

    @Test
    fun `personalized greeting uses fake implementation`() {
        val result = repository.getPersonalizedGreeting("Test User")
        assertThat(result).isEqualTo("Test greeting for: Test User")
    }

    /**
     * Test module that replaces the production AppModule.
     * Provides fake implementations for testing.
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object TestAppModule {
        
        @Provides
        @Singleton
        fun provideGreetingRepository(): GreetingRepository = FakeGreetingRepository()
    }
}

/**
 * Fake implementation for testing.
 */
class FakeGreetingRepository : GreetingRepository {
    var greetingToReturn = "Test Greeting!"
    var personalizedPrefix = "Test greeting for: "
    
    override fun getGreeting(): String = greetingToReturn
    
    override fun getPersonalizedGreeting(name: String): String = 
        "$personalizedPrefix$name"
}
