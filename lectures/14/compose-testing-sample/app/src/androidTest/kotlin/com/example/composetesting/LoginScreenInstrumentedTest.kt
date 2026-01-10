package com.example.composetesting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

@org.junit.Ignore("Fails on Android 16+ due to Espresso upstream bug. Use LoginScreenLocalTest instead.")
class LoginScreenInstrumentedTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<LoginActivity>()

  @Test
  fun login_flow_instrumented() {
    // Verify title
    composeTestRule
      .onNodeWithTag("title")
      .assertIsDisplayed()

    // Perform login
    composeTestRule
      .onNodeWithTag("emailInput")
      .performTextInput("user@instrumented.com")

    composeTestRule
      .onNodeWithTag("passwordInput")
      .performTextInput("securePass")

    composeTestRule
      .onNodeWithTag("loginButton")
      .performClick()

    // Verify success
    composeTestRule
      .onNodeWithTag("successMessage")
      .assertIsDisplayed()
  }
}
