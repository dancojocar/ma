package com.example.test

import android.widget.TextView
import com.example.R
import com.example.activity.SimpleActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [25])
class SimpleActivityTest {

  @Test
  fun testSomething() {

    val activity = Robolectric.setupActivity(SimpleActivity::class.java)
    assertTrue(activity != null)

    val textView = activity.findViewById(R.id.text) as TextView
    assertThat(textView.text).isEqualTo("Hello, Kotlin!")
  }
}
