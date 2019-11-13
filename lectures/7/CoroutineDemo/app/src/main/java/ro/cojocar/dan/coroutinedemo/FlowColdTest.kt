package ro.cojocar.dan.coroutinedemo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

fun fooCold(): Flow<Int> = flow {
  println("Flow started")
  for (i in 1..3) {
    delay(100)
    emit(i)
  }
}
fun main() = runBlocking<Unit> {
  println("Calling foo...")
  val flow = fooCold()
  println("Calling collect...")
  flow.collect { value -> println(value) }
  println("Calling collect again...")
  flow.collect { value -> println(value) }
}