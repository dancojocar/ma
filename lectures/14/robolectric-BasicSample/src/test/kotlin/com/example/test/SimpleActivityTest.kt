package com.example.test

import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import com.example.R
import com.example.activity.SimpleActivity
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
class SimpleActivityTest {

  @Test
  fun testSomething() {
    // GIVEN
    ActivityScenario.launch(SimpleActivity::class.java).use {
      it.onActivity {
        //WHEN
        val tv = it.findViewById<TextView>(R.id.text)
        //THEN
        assertThat(tv.text).isEqualTo("Hello, Kotlin!")
      }
    }
  }
}
