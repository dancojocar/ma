package com.example.composetesting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], instrumentedPackages = ["androidx.loader.content.ModernAsyncTask"])
class LoginScreenLocalTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun login_invalidEmail_showsError() {
    composeTestRule.setContent {
      LoginScreen()
    }

    // Enter invalid email
    composeTestRule
      .onNodeWithTag("emailInput")
      .performTextInput("invalid-email")

    // Click login
    composeTestRule
      .onNodeWithTag("loginButton")
      .performClick()

    // Verify error
    composeTestRule
      .onNodeWithTag("errorMessage")
      .assertIsDisplayed()
      .assertTextEquals("Invalid email")
  }

  @Test
  fun login_shortPassword_showsError() {
    composeTestRule.setContent {
      LoginScreen()
    }

    composeTestRule
      .onNodeWithTag("emailInput")
      .performTextInput("valid@test.com")

    composeTestRule
      .onNodeWithTag("passwordInput")
      .performTextInput("123")

    composeTestRule
      .onNodeWithTag("loginButton")
      .performClick()

    composeTestRule
      .onNodeWithTag("errorMessage")
      .assertIsDisplayed()
      .assertTextEquals("Password too short")
  }

  @Test
  fun login_success() {
    composeTestRule.setContent {
      LoginScreen()
    }

    composeTestRule
      .onNodeWithTag("emailInput")
      .performTextInput("valid@test.com")

    composeTestRule
      .onNodeWithTag("passwordInput")
      .performTextInput("password123")

    composeTestRule
      .onNodeWithTag("loginButton")
      .performClick()

    composeTestRule
      .onNodeWithTag("successMessage")
      .assertIsDisplayed()
  }
}
