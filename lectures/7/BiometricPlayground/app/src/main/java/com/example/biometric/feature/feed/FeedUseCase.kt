package com.example.biometric.feature.feed

import com.example.biometric.data.AuthRepository
import com.example.biometric.data.FeedItem
import com.example.biometric.data.FeedRepository
import javax.inject.Inject

class FeedUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val feedRepository: FeedRepository
) {
    suspend fun getFeed(): List<FeedItem> = authRepository.callWithAuthToken(feedRepository::getFeed)
}