package ro.cojocar.dan.rxlifecycle

import android.os.Bundle
import android.view.View
import com.trello.rxlifecycle4.android.ActivityEvent
import com.trello.rxlifecycle4.components.support.RxAppCompatActivity
import com.trello.rxlifecycle4.kotlin.bindToLifecycle
import com.trello.rxlifecycle4.kotlin.bindUntilEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import ro.cojocar.dan.rxlifecycle.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit

class MainActivity : RxAppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private val disposables = CompositeDisposable()

  // Configuration for different lifecycle demonstrations
  private val lifecycleDemos = listOf(
    LifecycleDemo(
      name = "onCreate -> onPause",
      event = ActivityEvent.PAUSE,
      description = "Binds until PAUSE event"
    ),
    LifecycleDemo(
      name = "onStart -> onStop",
      event = null, // Uses bindToLifecycle automatically
      description = "Automatically binds to opposite lifecycle (onStop)"
    ),
    LifecycleDemo(
      name = "onResume -> onDestroy",
      event = ActivityEvent.DESTROY,
      description = "Binds until DESTROY event"
    )
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupUI()
    startLifecycleDemos()
    logd("onCreate() - Started all lifecycle demonstrations")
  }

  private fun setupUI() {
    binding.apply {
      // Add headers for each demo section
      lifecycleDemos.forEach { demo ->
        textView.append("\n=== ${demo.name} ===\n")
        textView.append("${demo.description}\n\n")
      }
    }
  }

  private fun startLifecycleDemos() {
    startOnCreateDemo()
    // onStart and onResume demos will be started in their respective lifecycle methods
  }

  private fun startOnCreateDemo() {
    val demo = lifecycleDemos[0]
    Observable.interval(1, TimeUnit.SECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe {
        appendLogToTextView("${demo.name}: Subscribed", demo.name)
      }
      .doOnDispose {
        appendLogToTextView("${demo.name}: Unsubscribed", demo.name)
      }
      .bindUntilEvent(this, demo.event!!)
      .subscribe { num: Long ->
        appendLogToTextView("${demo.name}: $num", demo.name)
      }
      .addTo(disposables)
  }

  override fun onStart() {
    super.onStart()
    logd("onStart()")
    startOnStartDemo()
  }

  private fun startOnStartDemo() {
    val demo = lifecycleDemos[1]
    Observable.interval(1, TimeUnit.SECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe {
        appendLogToTextView("${demo.name}: Subscribed", demo.name)
      }
      .doOnDispose {
        appendLogToTextView("${demo.name}: Unsubscribed", demo.name)
      }
      .bindToLifecycle(this)
      .subscribe { num: Long ->
        appendLogToTextView("${demo.name}: $num", demo.name)
      }
      .addTo(disposables)
  }

  override fun onResume() {
    super.onResume()
    logd("onResume()")
    startOnResumeDemo()
  }

  private fun startOnResumeDemo() {
    val demo = lifecycleDemos[2]
    Observable.interval(1, TimeUnit.SECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .doOnSubscribe {
        appendLogToTextView("${demo.name}: Subscribed", demo.name)
      }
      .doOnDispose {
        appendLogToTextView("${demo.name}: Unsubscribed", demo.name)
      }
      .bindUntilEvent(this, demo.event!!)
      .subscribe { num: Long ->
        appendLogToTextView("${demo.name}: $num", demo.name)
      }
      .addTo(disposables)
  }

  override fun onPause() {
    super.onPause()
    logd("onPause()")
    // Log when pause happens but let RxLifecycle handle unsubscription
    appendLogToTextView("onPause() called - onCreate subscription should unsubscribe")
  }

  override fun onStop() {
    super.onStop()
    logd("onStop()")
    appendLogToTextView("onStop() called - onStart subscription should unsubscribe")
  }

  override fun onDestroy() {
    // Clear all disposables (though RxLifecycle should handle most)
    disposables.dispose()
    logd("onDestroy() - All disposables cleared")
    super.onDestroy()
  }

  private fun appendLogToTextView(message: String, tag: String? = null) {
    logi("$tag: $message")

    runOnUiThread {
      binding.apply {
        // Add timestamp for better debugging
        val timestamp = System.currentTimeMillis() % 10000
        val formattedMessage = if (tag != null) {
          "[$tag] $message (t+${timestamp}ms)"
        } else {
          "$message (t+${timestamp}ms)"
        }

        textView.append("$formattedMessage\n")

        // Auto-scroll to bottom
        scrollView.post {
          scrollView.fullScroll(View.FOCUS_DOWN)
        }
      }
    }
  }

  // Extension function to easily add disposables to CompositeDisposable
  private fun io.reactivex.rxjava3.disposables.Disposable.addTo(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
  }
}

// Data class to organize lifecycle demo configurations
data class LifecycleDemo(
  val name: String,
  val event: ActivityEvent?,
  val description: String
)