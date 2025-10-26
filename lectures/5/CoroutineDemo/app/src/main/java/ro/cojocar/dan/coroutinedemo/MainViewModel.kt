package ro.cojocar.dan.coroutinedemo

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.thread
import kotlin.random.Random

class MainViewModel : ViewModel() {

    val resultText = mutableStateOf("")
    val workers = mutableIntStateOf(100)
    val progressVisible = mutableStateOf(false)

    private var delayArray = initDelay(workers.intValue)

    fun onWorkersChange(workers: Int) {
        this.workers.intValue = workers
        delayArray = initDelay(workers)
    }

    fun onCoroutinesClick() {
        progressVisible.value = true
        viewModelScope.launch {
            captureRunningTimes("coroutines") {
                createCoroutines(delayArray)
            }
        }
    }

    fun onThreadsClick() {
        progressVisible.value = true
        viewModelScope.launch {
            captureRunningTimes("threads") {
                withContext(Dispatchers.IO) {
                    createThreads(delayArray)
                }
            }
        }
    }

    private fun initDelay(numberOfWorkers: Int): LongArray {
        val delayArray = LongArray(numberOfWorkers)
        val random = Random(numberOfWorkers)
        for (i in 0 until numberOfWorkers) {
            delayArray[i] = random.nextLong(4000, 5000)
        }
        return delayArray
    }


    private suspend fun captureRunningTimes(name: String, block: suspend () -> Unit) {
        val start = System.currentTimeMillis() / 1000
        resultText.value += "\nStarting $name at $start"
        block()
        val end = System.currentTimeMillis() / 1000
        resultText.value += "\nEnding at $end"
        resultText.value += "\nDiff: ${end - start}"
    }

    private suspend fun createCoroutines(delayArray: LongArray) {
        try {
            logd("Started")
            val jobs = List(delayArray.size) {
                viewModelScope.launch(Dispatchers.IO) {
                    delay(delayArray[it])
                }
            }
            jobs.forEach { it.join() }
            logd("Completed: ${jobs.size}")
        } catch (e: Exception) {
            loge("Error while executing the workers", e)
        } finally {
            progressVisible.value = false
        }
    }

    private fun createThreads(delayArray: LongArray) {
        try {
            logd("Started")
            val jobs = delayArray.map { delay ->
                thread {
                    Thread.sleep(delay)
                }
            }
            jobs.forEach { it.join() }
            logd("Completed: ${jobs.size}")
        } catch (e: Exception) {
            loge("Error while executing the workers", e)
        } finally {
            viewModelScope.launch {
                progressVisible.value = false
            }
        }
    }
}
