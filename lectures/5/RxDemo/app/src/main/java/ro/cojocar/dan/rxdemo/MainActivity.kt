package ro.cojocar.dan.rxdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import ro.cojocar.dan.rxdemo.ui.theme.RxDemoTheme
import java.lang.Thread.sleep
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
  private var textState by mutableStateOf("")
  private var progressState by mutableStateOf(false)
  private var searchTextState by mutableStateOf("")
  private var useDebounce by mutableStateOf(false) // Toggle state

  // RxJava subject for handling search with debounce
  private val searchSubject = PublishSubject.create<String>()
  private val disposables = CompositeDisposable()
  private var searchDisposable: Disposable? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RxDemoTheme {
        MainScreen(
          textState = textState,
          progressState = progressState,
          searchTextState = searchTextState,
          useDebounce = useDebounce,
          onSearchTextChanged = { searchText ->
            searchTextState = searchText
            searchSubject.onNext(searchText)
          },
          onUseDebounceChanged = { newValue ->
            useDebounce = newValue
            // Recreate the search observable with new debounce setting
            setupSearchObservable()
            // Clear previous search results when toggling
            textState = textState.split("\n\nFetch result:").firstOrNull() ?: ""
          }
        )
      }
    }

    setupSearchObservable()
    helloWorld()
    fromOperator()
    fetch()
  }

  private fun setupSearchObservable() {
    // Dispose previous subscription if it exists
    searchDisposable?.dispose()

    val baseObservable = searchSubject
      .doOnNext { searchTerm ->
        // Immediate feedback - show what was typed (without debounce)
        if (!useDebounce && searchTerm.isNotBlank()) {
          runOnUiThread {
            textState += "\nImmediate typing: '$searchTerm'"
          }
        }
      }

    // Conditionally apply debounce based on current toggle state
    val finalObservable = if (useDebounce) {
      baseObservable.debounce(500, TimeUnit.MILLISECONDS)
    } else {
      baseObservable
    }

    searchDisposable = finalObservable
      .distinctUntilChanged() // Only emit if different from previous
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe { searchTerm ->
        if (searchTerm.isNotBlank()) {
          if (useDebounce) {
            textState += "\n\n[DEBOUNCE] Search result for: '$searchTerm'"
            logd("[DEBOUNCE] From search Action $searchTerm")
          } else {
            textState += "\n\n[NO DEBOUNCE] Search result for: '$searchTerm'"
            logd("[NO DEBOUNCE] From search Action $searchTerm")
          }
        }
      }

    searchDisposable?.let { disposables.add(it) }
  }

  private fun helloWorld() {
    val myObservable = Observable.just("Hello")
    // Emits "Hello"

    val myObserver = object : Observer<String> {
      override fun onComplete() {
        // Called when the observable has no more data to emit
        textState += "\nHelloWorld Completed\n\n"
      }

      override fun onError(e: Throwable) {
        toast("Received an error while processing HelloWorld")
        loge("Unknown error", e)
      }

      override fun onSubscribe(d: Disposable) {
        // CompositeDisposable is used to dispose all the subscriptions at once
        disposables.add(d)
        logd("Subscribed")
      }

      override fun onNext(message: String) {
        // Called each time the observable emits data
        textState += "\nMessages: $message"
        logd("Received: $message")
      }
    }
    myObservable.subscribe(myObserver)
  }

  private fun fromOperator() {
    var myArrayObservable = Observable.fromArray(1, 2, 3, 4, 5, 6)
    // Emits each item of the array, one at a time

    myArrayObservable = myArrayObservable.map {
      // Square the number
      it * it
    }

    myArrayObservable = myArrayObservable
      .skip(2) // Skip the first two items
      .filter {
        // Ignores any item that returns false
        it % 2 == 0
      }

    val disposable = myArrayObservable.subscribe {
      textState += "\nOperator: $it"
      logd("From Action $it")
    } // Prints the number received
    disposables.add(disposable)
  }

  private fun fetch() {
    // Observable.fromCallable is better than Observable.create because it handles
    // the case where the function throws an exception.
    val fetchFromGoogle = Observable.fromCallable {
      fetchData("http://www.google.com")
    }
    val fetchFromYahoo = Observable.fromCallable {
      fetchData("http://www.yahoo.com")
    }

    progressState = true
    // Fetch from both simultaneously
    val zipped = Observable.zip(
      fetchFromGoogle.onErrorReturn {
        "Error from Google: ${it.message}"
      },
      fetchFromYahoo.onErrorReturn {
        "Error from Yahoo: ${it.message}"
      }
    ) { google, yahoo ->
      // Do something with the results of both threads
      google + "\n" + yahoo
    }

    val disposable = zipped
      // Schedulers.newThread() is used to run the zip operator on a new thread.
      .subscribeOn(Schedulers.newThread())
      // AndroidSchedulers.mainThread() is used to run the onNext, onError and onComplete
      // on the main thread.
      .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
      .subscribe({
        textState += "\n\nFetch result:\n$it"
        logd("received from google and yahoo: $it ")
      }, {
        toast("Received an error while fetching data")
        loge("Error on fetching", it)
      }, {
        progressState = false
      })
    disposables.add(disposable)
  }

  private fun fetchData(url: String): String {
    val rand = Random()
    val sec = rand.nextInt(5)
    try {
      logd("$url is sleeping $sec sec")
      sleep((sec * 1000).toLong())
    } catch (e: InterruptedException) {
      loge("Interrupted while sleeping :(", e)
    }
    return url
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }
}

@Composable
fun MainScreen(
  textState: String,
  progressState: Boolean,
  searchTextState: String,
  useDebounce: Boolean,
  onSearchTextChanged: (String) -> Unit,
  onUseDebounceChanged: (Boolean) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.safeDrawing)
      .padding(16.dp)
  ) {
    // Toggle button for debounce
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    ) {
      Text(
        text = "Use Debounce (500ms)",
        modifier = Modifier.weight(1f)
      )
      Switch(
        checked = useDebounce,
        onCheckedChange = onUseDebounceChanged
      )
    }

    // Status indicator
    Text(
      text = if (useDebounce) "🔵 DEBOUNCE ENABLED - typing will wait 500ms"
      else "🔴 DEBOUNCE DISABLED - immediate results",
      style = MaterialTheme.typography.bodySmall,
      color = if (useDebounce) MaterialTheme.colorScheme.primary
      else MaterialTheme.colorScheme.error,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    OutlinedTextField(
      value = searchTextState,
      onValueChange = onSearchTextChanged,
      label = { Text(stringResource(id = R.string.search)) },
      placeholder = { Text("Type to see the difference...") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    if (progressState) {
      CircularProgressIndicator()
    }

    // Scrollable text area
    Text(
      text = textState,
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    )
  }
}