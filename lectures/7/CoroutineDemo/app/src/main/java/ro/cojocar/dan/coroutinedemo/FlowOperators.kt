package ro.cojocar.dan.coroutinedemo

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

suspend fun performRequest(request: Int): String {
  delay(1000) // imitate long-running asynchronous work
  return "response $request"
}

fun main() = runBlocking<Unit> {
  (1..3).asFlow() // a flow of requests
    .map { request -> performRequest(request) }
    .collect { response -> println(response) }
}
