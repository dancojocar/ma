package com.example.hilttesting

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Singleton

/**
 * Instrumented UI test with Hilt dependency injection.
 *
 * Demonstrates:
 * - @HiltAndroidTest for Hilt-enabled UI tests
 * - Module replacement for controlled testing
 * - Compose testing with injected ViewModels
 */
@HiltAndroidTest
@UninstallModules(AppModule::class)
@org.junit.Ignore("Fails on Android 16+ due to Espresso upstream bug. Use GreetingScreenHiltLocalTest instead.")
class GreetingScreenHiltTest {

  @get:Rule(order = 0)
  val hiltRule = HiltAndroidRule(this)

  @get:Rule(order = 1)
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Before
  fun setup() {
    hiltRule.inject()
  }

  @Test
  fun loadGreeting_displaysTestGreeting() {
    // Click the default greeting button
    composeTestRule
      .onNodeWithTag("loadGreetingButton")
      .performClick()

    // Verify the fake greeting is displayed
    composeTestRule
      .onNodeWithTag("greetingText")
      .assertTextEquals("Instrumented Test Greeting!")
  }

  @Test
  fun personalizedGreeting_displaysWithName() {
    // Enter a name
    composeTestRule
      .onNodeWithTag("nameInput")
      .performTextInput("Android")

    // Click personalized button
    composeTestRule
      .onNodeWithTag("personalizedButton")
      .performClick()

    // Verify personalized greeting
    composeTestRule
      .onNodeWithTag("greetingText")
      .assertTextContains("Android")
  }

  /**
   * Test module for instrumented tests.
   */
  @Module
  @InstallIn(SingletonComponent::class)
  object TestAppModule {

    @Provides
    @Singleton
    fun provideGreetingRepository(): GreetingRepository = object : GreetingRepository {
      override fun getGreeting(): String = "Instrumented Test Greeting!"
      override fun getPersonalizedGreeting(name: String): String =
        "Hello from test, $name!"
    }
  }
}
