package com.example.dan.memoryleakdemo

import android.app.Activity
import android.content.Intent
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.runner.Runner

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
  @Test
  fun useAppContext() {
    // Context of the app under test.
    val appContext = InstrumentationRegistry.getTargetContext()
    assertEquals("com.example.dan.memoryleakdemo", appContext.packageName)
  }
}

//SCAFFOLDING

@RunWith(AndroidJUnit4::class)
class SimpleUnifiedTest {
  @Before
  fun setup() {
    val context = InstrumentationRegistry.getTargetContext()
    context.toString()
  }
}

val view: View
//THEN

@RunWith(AndroidJUnit4::class)
class SimpleUnifiedTest {
  @Test
  fun testVisibleView() {
    assertThat(view).isVisible()
  }
}


//WHEN

@RunWith(AndroidJUnit4::class)
class SimpleUnifiedTest {
  @Test
  fun testButtonClickSendsIntent() {
    onView(withId(R.id.fab)).perform(click())

    intended(hasAction(equalTo("android.intent.action.EDIT")))
  }
}

//GIVEN

@RunWith(AndroidJUnit4::class)
class SimpleUnifiedTest {
  @get:Rule
  val rule = ActivityTestRule(NoteListActivity::class.java)

  @Test
  fun testMotionEvents() {
    val motionEvent = buildMotionEvent().setAction(MotionEvent.ACTION_DOWN)
  }

  private fun buildMotionEvent(): Any {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}

private fun Any.setAction(actioN_DOWN: Int): Any {
  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}


@RunWith(AndroidJUnit4::class)
class OnDeviceTest {

  @get:Rule
  val rule = ActivityTestRule(NoteListActivity::class.java)

  @Test
  fun testTitle() {
    onView(withId(R.id.fab)).perform(click())

    intended(hasAction(equalTo("android.intent.action.EDIT")))
  }

  private fun hasAction(equalTo: Matcher<String>?): Any {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun intended(hasAction: Any) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}

class ActivityTestRule(java: Class<NoteListActivity>) {

}


@RunWith(RobolecticTestRunner::class)
class RobolecticTest {

  @Test
  fun testTitle() {
    val activity = Roboelectic.setupActivity(NoteListActivity::class.java)

    ShadowView.clickOn(activity.findViewById(R.id.title))

    assertEquals(
        ShadowApplication.getInstance().peekNextStartedActivity().action,
        "android.intend.action.EDIT"
    )
  }
}

private val Any.action: Any?
  get() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

class ShadowApplication {
  companion object {
    fun getInstance(): ShadowApplicationStartedActivity {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
  }
}

class ShadowApplicationStartedActivity {
  fun peekNextStartedActivity(): Any {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

}

class ShadowView {
  companion object {
    fun clickOn(findViewById: Any) {

    }
  }
}

private fun Unit.findViewById(title: Int): Any {
  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

class Roboelectic {
  companion object {
    fun setupActivity(java: Class<NoteListActivity>) {

    }
  }
}

class RobolecticTestRunner : Runner {

}


@RunWith(MockitoJUnitRunner::class)
class MockitoTest {
  @Spy
  var spyActivity = NoteListActivity()
  @Captor
  lateinit var intentCaptor: ArgumentCaptor<Intent>
  @Captor
  lateinit var clickCaptor: ArgumentCaptor<NoteListActivity.ClickHandler>

  fun testTitle() {
    `when`(spyActivity.findViewById(R.id.title)).thenReturn(mock<TextView>())

    clickCaptor.value.click()

    verify(spyActivity).startActivity(intentCaptor.capture())
  }

  private fun verify(spyActivity: NoteListActivity) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun <T> mock(): T {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun `when`(findViewById: Any): Any {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}

annotation class Captor

annotation class Spy

private fun Any.startActivity(capture: Any) {
  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

private fun TextView?.click() {
  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

private fun Any.thenReturn(mock: Any) {
  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

class ArgumentCaptor<T> {
  fun capture(): T {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  val value: TextView? = null

}

class NoteListActivity : Activity() {
  class ClickHandler {

  }

}

class MockitoJUnitRunner : Runner {

}
