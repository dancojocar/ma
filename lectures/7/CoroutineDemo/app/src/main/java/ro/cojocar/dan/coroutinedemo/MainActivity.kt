package ro.cojocar.dan.coroutinedemo

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.random.Random

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    var numberOfWorkers = 1_000
    var delayArray = initDelay(numberOfWorkers)
    workers.text = "$numberOfWorkers"
    seekBar.min = numberOfWorkers
    seekBar.max = 1_000_000
    seekBar.progress = numberOfWorkers
    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        numberOfWorkers = progress
        workers.text = "$numberOfWorkers"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
      }

      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        delayArray = initDelay(numberOfWorkers)
      }
    })

//    logd(delayArray.contentToString())

    buttonCoroutines.setOnClickListener {
      myProgress.visibility = View.VISIBLE
      captureRunningTimes("coroutines") {
        runBlocking {
          createCoroutines(delayArray)
        }
      }
    }
    buttonThreads.setOnClickListener {
      myProgress.visibility = View.VISIBLE
      captureRunningTimes("threads") {
        createThreads(delayArray)
      }
    }

    val fibonacci = sequence {
      var current = 1
      var next = 1
      while (true) {
        yield(current)
        logd("Fib body")
        val new = current + next
        current = next
        next = new
      }
    }

    val iterator = fibonacci.iterator()
    buttonFibonacci.setOnClickListener {
      textView.text = "${textView.text}\nFib: ${iterator.next()}"
    }


  }

  private fun initDelay(numberOfWorkers: Int): LongArray {
    val delayArray = LongArray(numberOfWorkers)
    val random = Random(numberOfWorkers)
    for (i in 0 until numberOfWorkers) {
      delayArray[i] = random.nextLong(3000)
    }
    return delayArray
  }


  private fun captureRunningTimes(name: String, block: () -> Unit) {
    val start = System.currentTimeMillis() / 1000
    textView.text = "${textView.text}\n\n${name}\nStart: $start"
    block()
    val end = System.currentTimeMillis() / 1000
    textView.text = "${textView.text}\n  End: $end"
    textView.text = "${textView.text}\nDiff: ${end - start}"
  }

  private suspend fun createCoroutines(delayArray: LongArray) {
    try {
      coroutineScope {
        logd("Started")
        val jobs = List(delayArray.size) {
          launch {
            delay(delayArray[it])
          }
        }
        jobs.forEach { it.join() }
        logd("Completed: ${jobs.size}")
      }
    } catch (e: Exception) {
      loge("Error while executing the workers", e)
    } finally {
      myProgress.visibility = View.GONE
    }
  }

  private fun createThreads(delayArray: LongArray) {
    try {
      logd("Started")
      val jobs = List(delayArray.size) {
        thread {
          sleep(delayArray[it])
        }
      }
      jobs.forEach { it.join() }
      logd("Completed: ${jobs.size}")
    } catch (e: Exception) {
      loge("Error while executing the workers", e)
    } finally {
      myProgress.visibility = View.GONE
    }
  }
}
