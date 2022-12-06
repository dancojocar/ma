package com.example.biometric.data

import com.example.biometric.data.model.UserToken
import com.example.biometric.data.remote.FAKE_TOKEN
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FeedRepository @Inject constructor() {
    suspend fun getFeed(userToken: UserToken): List<FeedItem> {
        delay(1500)

        return if (userToken.value == FAKE_TOKEN) {
            List(20) {
                FeedItem("Title $it", "Description $it")
            }
        } else {
            throw IllegalStateException("Not authorized")
        }
    }
}

data class FeedItem(val title: String, val description: String)