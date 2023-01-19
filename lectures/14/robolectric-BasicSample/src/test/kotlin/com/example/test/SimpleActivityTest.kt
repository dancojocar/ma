package com.example.test

import android.widget.TextView
import com.example.R
import com.example.activity.SimpleActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric.buildActivity
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class SimpleActivityTest {

  @Test
  fun testSomething() {
    // GIVEN
    val controller = buildActivity(SimpleActivity::class.java).setup()

    // WHEN
    controller.pause().stop()
    val activity: SimpleActivity = controller.get()
    val tv = activity.findViewById<TextView>(R.id.text)

    //THEN
    assertThat(tv.text).isEqualTo("Hello, Kotlin!")
  }
}
