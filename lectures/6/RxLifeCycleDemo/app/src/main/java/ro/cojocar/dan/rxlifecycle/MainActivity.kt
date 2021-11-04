package ro.cojocar.dan.rxlifecycle

import android.os.Bundle
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Observable
import ro.cojocar.dan.rxlifecycle.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : RxAppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Specifically bind this until onPause()
    Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { logi("Unsubscribing subscription from onCreate()") }
      .bindUntilEvent(this, ActivityEvent.PAUSE)
      .subscribe { num -> logi("Started in onCreate(), running until onPause(): $num") }
  }

  override fun onStart() {
    super.onStart()
    logd("onStart()")
    // Using automatic unsubscribe, this should determine that the correct time to
    // unsubscribe is onStop (the opposite of onStart).
    Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { logi("Unsubscribing subscription from onStart()") }
      .bindToLifecycle(this)
      .subscribe { num -> logi("Started in onStart(), running until in onStop(): $num") }
  }

  override fun onResume() {
    super.onResume()
    logd("onResume()")
    Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { logi("Unsubscribing subscription from onResume()") }
      .bindUntilEvent(this, ActivityEvent.DESTROY)
      .subscribe { num -> logi("Started in onResume(), running until in onDestroy(): $num") }
  }

  override fun onPause() {
    super.onPause()
    logd("onPause()")
  }

  override fun onStop() {
    super.onStop()
    logd("onStop()")
  }

  override fun onDestroy() {
    super.onDestroy()
    logd("onDestroy()")
  }
}
