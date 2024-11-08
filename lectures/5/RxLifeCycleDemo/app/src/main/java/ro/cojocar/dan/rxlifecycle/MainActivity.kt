package ro.cojocar.dan.rxlifecycle

import android.os.Bundle
import android.view.View
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import ro.cojocar.dan.rxlifecycle.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : RxAppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  private lateinit var onCreateSubscription: Disposable
  private lateinit var onStartSubscription: Disposable
  private lateinit var onResumeSubscription: Disposable

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Specifically bind this until onPause()
    onCreateSubscription = Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { appendLogToTextView("Unsubscribing subscription from onCreate()") }
      .bindUntilEvent(this, ActivityEvent.PAUSE)
      .subscribe { num -> appendLogToTextView("Started in onCreate(), running until onPause(): $num") }
  }

  override fun onStart() {
    super.onStart()
    logd("onStart()")
    // Using automatic unsubscribe, this should determine that the correct time to
    // unsubscribe is onStop (the opposite of onStart).
    onStartSubscription = Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { appendLogToTextView("Unsubscribing subscription from onStart()") }
      .bindToLifecycle(this)
      .subscribe { num -> appendLogToTextView("Started in onStart(), running until in onStop(): $num") }
  }

  override fun onResume() {
    super.onResume()
    logd("onResume()")
    onResumeSubscription = Observable.interval(1, TimeUnit.SECONDS)
      .doOnDispose { appendLogToTextView("Unsubscribing subscription from onResume()") }
      .bindUntilEvent(this, ActivityEvent.DESTROY)
      .subscribe { num -> appendLogToTextView("Started in onResume(), running until in onDestroy(): $num") }
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

  private fun appendLogToTextView(message: String) {
    logi(message)
    // Ensure this code runs on the main (UI) thread
    runOnUiThread {
      binding.textView.append("$message\n")
      binding.scrollView.post { binding.scrollView.fullScroll(View.FOCUS_DOWN) }
    }
  }
}
