package ro.cojocar.dan.coroutinedemo

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import ro.cojocar.dan.coroutinedemo.databinding.ActivityMainBinding
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.random.Random

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    var numberOfWorkers = 1_000
    var delayArray = initDelay(numberOfWorkers)
    binding.workers.text = "$numberOfWorkers"
    binding.seekBar.min = numberOfWorkers
    binding.seekBar.max = 10_000
    binding.seekBar.progress = numberOfWorkers
    binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        numberOfWorkers = progress
        binding.workers.text = "$numberOfWorkers"
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
      }

      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        delayArray = initDelay(numberOfWorkers)
      }
    })

//    logd(delayArray.contentToString())

    binding.buttonCoroutines.setOnClickListener {
      binding.myProgress.visibility = View.VISIBLE
      launch {
        captureRunningTimes("coroutines") {
          runBlocking {
            createCoroutines(delayArray)
          }
        }
      }
    }
    binding.buttonThreads.setOnClickListener {
      binding.myProgress.visibility = View.VISIBLE
      captureRunningTimes("threads") {
        createThreads(delayArray)
      }
    }

    val fibonacci: Sequence<Long> = sequence {
      var current = 1L
      var next = 1L
      while (true) {
        yield(current)
        logd("Fib body")
        val new = current + next
        current = next
        next = new
      }
    }

    val iterator = fibonacci.iterator()
    binding.buttonFibonacci.setOnClickListener {
      binding.textView.text = "${binding.textView.text}\nFib: ${iterator.next()}"
      binding.scrollView.fullScroll(View.FOCUS_DOWN)
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
    binding.textView.text = "${binding.textView.text}\n\n${name}\nStart: $start"
    block()
    val end = System.currentTimeMillis() / 1000
    binding.textView.text = "${binding.textView.text}\n  End: $end"
    binding.textView.text = "${binding.textView.text}\nDiff: ${end - start}"
    binding.scrollView.fullScroll(View.FOCUS_DOWN)
  }

  private suspend fun createCoroutines(delayArray: LongArray) {
    try {
      coroutineScope {
        logd("Started")
        val jobs = List(delayArray.size) {
          launch {
            withContext(Dispatchers.IO) {
              delay(delayArray[it])
            }
          }
        }
        jobs.forEach { it.join() }
        logd("Completed: ${jobs.size}")
      }
    } catch (e: Exception) {
      loge("Error while executing the workers", e)
    } finally {
      binding.myProgress.visibility = View.GONE
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
      binding.myProgress.visibility = View.GONE
    }
  }
}
