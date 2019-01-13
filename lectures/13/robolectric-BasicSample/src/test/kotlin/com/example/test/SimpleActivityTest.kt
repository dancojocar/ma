package com.example.test

import com.example.BuildConfig
import com.example.activity.SimpleActivity

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

import org.junit.Assert.assertTrue
import org.robolectric.RobolectricTestRunner
import android.widget.TextView
import com.example.R

import org.assertj.core.api.Assertions.assertThat

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = [25])
class SimpleActivityTest {

  @Test
  fun testSomething() {
    val activity = Robolectric.setupActivity(SimpleActivity::class.java)
    assertTrue(activity != null)

    val textView = activity.findViewById(R.id.text) as TextView
    assertThat(textView.text).isEqualTo("Hello, Kotlin!")
  }
}
