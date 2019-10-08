package ro.cojocar.dan.coroutinedemo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonCoroutines.setOnClickListener {
            captureRuningTimes { createCoroutines() }
        }
        buttonThreads.setOnClickListener {
            captureRuningTimes { createThreads() }
        }

        val fibonacci = sequence {
            var current = 1
            var next = 1
            while (true) {
                yield(current)
                this@MainActivity.logd("Fib body")
                val new = current + next
                current = next
                next = new
            }
        }

        val iter = fibonacci.iterator()
        buttonFibonacci.setOnClickListener {
            textView.text = "${textView.text}\nFib: ${iter.next()}"
        }
    }


    private fun captureRuningTimes(block: () -> Unit) {
        textView.text = "Start: ${System.currentTimeMillis() / 1000}"
        block()
        textView.text = "${textView.text}\n\n  End: ${System.currentTimeMillis() / 1000}"
    }

    private fun createCoroutines() {
        runBlocking {
            val jobs = List(100_000) {
                launch {
                    delay(3000L)
                    this@MainActivity.logd(".")
                }
            }
            jobs.forEach { it.join() }
        }
    }

    private fun createThreads() {
        runBlocking {
            val jobs = List(100_000) {
                thread {
                    Thread.sleep(3000L)
                    this@MainActivity.logd(".")
                }
            }
            jobs.forEach { it.join() }
        }
    }


}
