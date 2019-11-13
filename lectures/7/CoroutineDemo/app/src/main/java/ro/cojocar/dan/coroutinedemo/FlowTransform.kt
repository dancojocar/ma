package ro.cojocar.dan.coroutinedemo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.runBlocking

@ExperimentalCoroutinesApi
fun main() = runBlocking<Unit> {
  (1..3).asFlow() // a flow of requests
    .transform { request ->
      emit("Making request $request")
      emit(performRequest(request))
    }
    .collect { response -> println(response) }
}