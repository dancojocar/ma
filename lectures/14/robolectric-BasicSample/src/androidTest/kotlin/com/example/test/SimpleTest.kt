package com.example.test

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.activity.SimpleActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleTest {

  @get:Rule
  val activityTestRule = ActivityScenarioRule(SimpleActivity::class.java)


  @Test
  fun saysHello() {
    onView(withText("Hello, Kotlin!")).check(matches(isDisplayed()))
  }
}
