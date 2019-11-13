package ro.cojocar.dan.coroutinedemo

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
  launch {
    delay(100L)
    println("Task from runBlocking")
  }
  coroutineScope {
    // Creates a new coroutine scope
    launch {
      delay(400L)
      println("Task from nested launch")
    }
    delay(100L)
    println("Task from coroutine scope")
  }
  println("Coroutine scope is over")
}