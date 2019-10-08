package ro.cojocar.dan.rxdemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }


    fun start() {
        helloWorld()
        fromOperator()
        fetch()
    }

    private fun helloWorld() {
        val myObservable = Observable.just("Hello") // Emits "Hello"

        val myObserver = object : Observer<String> {
            override fun onComplete() {
                // Called when the observable has no more data to emit
                textView.text = "${textView.text}\nHelloWorld Completed"
            }

            override fun onError(e: Throwable) {
                loge("Unknown error", e)
            }

            override fun onSubscribe(d: Disposable) {
                logd("Subscribed")
            }

            override fun onNext(message: String) {
                // Called each time the observable emits data
                textView.text = "${textView.text}\nMessage: $message"
                logd("Received: $message")
            }
        }
        myObservable.subscribe(myObserver)
    }


    private fun fromOperator() {
        var myArrayObservable = Observable.fromArray(1, 2, 3, 4, 5, 6) // Emits each item of the array, one at a time

        myArrayObservable = myArrayObservable.map { it -> it * it } // Square the number

        myArrayObservable = myArrayObservable
            .skip(2) // Skip the first two items
            .filter { it % 2 == 0 } // Ignores any item that returns false

        disposable.add(myArrayObservable.subscribe {
            textView.text = "${textView.text}\nOp: $it"
            logd("From Action $it")
        }) // Prints the number received

    }


    private fun fetch() {
        val fetchFromGoogle = Observable.create(ObservableOnSubscribe<String> { emitter ->
            try {
                val data = fetchData("http://www.google.com")
                emitter.onNext(data) // Emit the contents of the URL
                emitter.onComplete() // Nothing more to emit
            } catch (e: Exception) {
                emitter.onError(e) // In case there are network errors
            }
        })

        val fetchFromYahoo = Observable.create(ObservableOnSubscribe<String> { emitter ->
            try {
                val data = fetchData("http://www.yahoo.com")
                emitter.onNext(data) // Emit the contents of the URL
                emitter.onComplete() // Nothing more to emit
            } catch (e: Exception) {
                emitter.onError(e) // In case there are network errors
            }
        })

        // Fetch from both simultaneously
        val zipped =
            Observable.zip(fetchFromGoogle, fetchFromYahoo, BiFunction<String, String, String> { google, yahoo ->
                // Do something with the results of both threads
                google + "\n" + yahoo
            })

        disposable.add(zipped.subscribeOn(Schedulers.newThread()) // Create a new Thread
            .observeOn(AndroidSchedulers.mainThread()) // Use the UI thread
            .subscribe {
                textView.text = "${textView.text}\nFetch result: $it"
                logd("received from google and yahoo: $it ")
            })
    }

    private fun fetchData(url: String): String {
        val rand = Random()
        val sec = rand.nextInt(5)
        try {
            logd("$url is sleeping $sec sec")
            Thread.sleep((sec * 1000).toLong())
        } catch (e: InterruptedException) {
            loge("Interrupted while sleeping :(", e)
        }
        return url
    }
}
