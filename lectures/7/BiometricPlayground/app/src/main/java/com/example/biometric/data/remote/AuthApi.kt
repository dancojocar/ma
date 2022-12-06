package com.example.biometric.data.remote

import com.example.biometric.data.model.UserToken
import javax.inject.Inject
import kotlinx.coroutines.delay

class AuthApi @Inject constructor() {
    suspend fun login(username: String, password: String): UserToken {
        delay(1000)

        return if (username == "test1234" && password == "test1234") {
            UserToken(FAKE_TOKEN)
        } else {
            throw LoginError
        }
    }
}

object LoginError : Throwable("Wrong credentials")