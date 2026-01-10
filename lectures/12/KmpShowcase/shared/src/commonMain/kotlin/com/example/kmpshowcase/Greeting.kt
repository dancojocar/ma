package com.example.kmpshowcase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.seconds

class Greeting {
    fun greet(): Flow<String> = flow {
        emit("Hello, " + Platform().platform + "!")
        delay(1.seconds)
        emit("Welcome to KMP!")
    }
}
