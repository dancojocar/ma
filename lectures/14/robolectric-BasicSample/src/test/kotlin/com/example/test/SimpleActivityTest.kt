package com.example.test

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.activity.SimpleScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests using Robolectric + Compose Testing.
 *
 * Demonstrates:
 * - Running Compose UI tests locally without an emulator
 * - Using Robolectric to simulate Android framework
 * - Testing Compose state changes
 * - Testing user interactions
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class SimpleActivityTest {

  @get:Rule
  val composeTestRule = androidx.compose.ui.test.junit4.createAndroidComposeRule<com.example.activity.SimpleActivity>()

  @Test
  fun greeting_isDisplayed() {
    // Verify greeting text is displayed
    composeTestRule
      .onNodeWithTag("greeting")
      .assertIsDisplayed()
      .assertTextEquals("Hello, Kotlin!")
  }

  @Test
  fun counter_startsAtZero() {
    composeTestRule
      .onNodeWithTag("counter")
      .assertTextEquals("Count: 0")
  }

  @Test
  fun incrementButton_increasesCounter() {
    composeTestRule
      .onNodeWithTag("incrementButton")
      .performClick()

    composeTestRule
      .onNodeWithTag("counter")
      .assertTextEquals("Count: 1")
  }

  @Test
  fun decrementButton_decreasesCounter() {
    composeTestRule
      .onNodeWithTag("decrementButton")
      .performClick()

    composeTestRule
      .onNodeWithTag("counter")
      .assertTextEquals("Count: -1")
  }

  @Test
  fun resetButton_resetsCounter() {
    // First increment
    composeTestRule
      .onNodeWithTag("incrementButton")
      .performClick()

    // Then reset
    composeTestRule
      .onNodeWithTag("resetButton")
      .performClick()

    composeTestRule
      .onNodeWithTag("counter")
      .assertTextEquals("Count: 0")
  }

  @Test
  fun allButtons_areClickable() {
    composeTestRule
      .onNodeWithTag("incrementButton")
      .assertHasClickAction()

    composeTestRule
      .onNodeWithTag("decrementButton")
      .assertHasClickAction()

    composeTestRule
      .onNodeWithTag("resetButton")
      .assertHasClickAction()
  }

}
