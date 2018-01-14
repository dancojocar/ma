package com.example.android.testing.espresso.BasicSample;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MyCustomTest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

  @Test
  public void myCustomTest() {
    ViewInteraction editText = onView(
        allOf(withId(R.id.editTextUserInput), isDisplayed()));
    editText.perform(replaceText("test12"), closeSoftKeyboard());

    ViewInteraction button = onView(
        allOf(withId(R.id.changeTextBt), withText("Change text"), isDisplayed()));
    button.perform(click());

    ViewInteraction button2 = onView(
        allOf(withId(R.id.activityChangeTextBtn), withText("Open activity and change text"), isDisplayed()));
    button2.perform(click());

  }

}
