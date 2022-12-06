package com.example.biometric.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.biometric.data.FeedItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FeedViewModel @Inject constructor(feedUseCase: FeedUseCase) : ViewModel() {
    val feed = flow { emit(feedUseCase.getFeed()) }
        .map<List<FeedItem>, FeedState> { FeedState.Success(it) }
        .catch { emit(FeedState.Failure(it.message)) }
        .stateIn(viewModelScope, SharingStarted.Lazily, FeedState.Loading)
}

sealed class FeedState {
    object Loading : FeedState()
    data class Success(val feed: List<FeedItem>) : FeedState()
    data class Failure(val message: String?) : FeedState()
}