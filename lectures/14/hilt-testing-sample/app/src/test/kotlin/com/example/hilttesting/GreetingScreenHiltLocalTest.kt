package com.example.hilttesting

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Singleton

/**
 * Local UI test with Hilt dependency injection using Robolectric.
 * 
 * Demonstrates:
 * - Running Hilt UI tests locally (fast, no emulator needed)
 * - @HiltAndroidTest with RobolectricTestRunner
 * - Module replacement for controlled testing
 */
@HiltAndroidTest
@Config(application = HiltTestApplication::class, sdk = [30], packageName = "com.example.hilttesting")
@UninstallModules(AppModule::class)
@RunWith(RobolectricTestRunner::class)
class GreetingScreenHiltLocalTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Use createAndroidComposeRule<MainActivity>() for proper Hilt support
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loadGreeting_displaysTestGreeting() {
        // MainActivity already calls setContent { GreetingScreen() }
        
        // Click the default greeting button
        composeTestRule
            .onNodeWithTag("loadGreetingButton")
            .performClick()

        // Verify the fake greeting is displayed
        composeTestRule
            .onNodeWithTag("greetingText")
            .assertTextEquals("Local Test Greeting!")
    }

    @Test
    fun personalizedGreeting_displaysWithName() {
        // MainActivity already calls setContent { GreetingScreen() }

        // Enter a name
        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Robolectric")

        // Click personalized button
        composeTestRule
            .onNodeWithTag("personalizedButton")
            .performClick()

        // Verify personalized greeting
        composeTestRule
            .onNodeWithTag("greetingText")
            .assertTextEquals("Hello from local test, Robolectric!")
    }

    /**
     * Test module for local tests.
     */
    @Module
    @InstallIn(SingletonComponent::class)
    object TestAppModule {
        
        @Provides
        @Singleton
        fun provideGreetingRepository(): GreetingRepository = object : GreetingRepository {
            override fun getGreeting(): String = "Local Test Greeting!"
            override fun getPersonalizedGreeting(name: String): String = 
                "Hello from local test, $name!"
        }
    }
}
