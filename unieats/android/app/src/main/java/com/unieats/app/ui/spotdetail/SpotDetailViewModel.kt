package com.unieats.app.ui.spotdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.unieats.shared.Review
import com.unieats.shared.Spot
import com.unieats.app.data.remote.AiDescribeClient
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.EatsRepository
import com.unieats.app.ui.navigation.SpotDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList(),
    val isPendingSync: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isLive: Boolean = false,
    val isReviewFormOpen: Boolean = false,
    val isSubmittingReview: Boolean = false,
    val reviewError: String? = null,
    val aiText: String = "",
    val isDescribing: Boolean = false,
    val aiError: String? = null
)

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: EatsRepository,
    private val aiClient: AiDescribeClient,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spotId: String = savedStateHandle.toRoute<SpotDetail>().spotId

    private val _uiState = MutableStateFlow(SpotDetailUiState())

    val uiState: StateFlow<SpotDetailUiState> = combine(
        _uiState,
        spotRepository.observeSpot(spotId),
        spotRepository.observeReviews(spotId),
        spotRepository.observePendingSpotIds(),
        spotRepository.liveConnection
    ) { state, spot, reviews, pendingIds, live ->
        state.copy(spot = spot, reviews = reviews, isPendingSync = spotId in pendingIds, isLive = live)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val spot = async { spotRepository.refreshSpot(spotId) }
            val reviews = async { spotRepository.refreshReviews(spotId) }
            val error = spot.await().exceptionOrNull() ?: reviews.await().exceptionOrNull()
            _uiState.update { it.copy(isLoading = false, errorMessage = error?.toUserMessage()) }
        }
    }

    private var describeJob: Job? = null

    /** Streams the AI description of the spot as currently shown (full spot, openNow included). */
    fun describeDish() {
        val spot = uiState.value.spot ?: return
        describeJob?.cancel()
        describeJob = viewModelScope.launch {
            _uiState.update { it.copy(aiText = "", isDescribing = true, aiError = null) }
            try {
                aiClient.describe(spot).collect { delta -> _uiState.update { it.copy(aiText = it.aiText + delta) } }
                _uiState.update { it.copy(isDescribing = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = when (e) {
                    is HttpRequestTimeoutException -> e.toUserMessage()
                    is IOException -> "Server unreachable. Is it running on port 3000?"
                    else -> e.toUserMessage()
                }
                _uiState.update { it.copy(isDescribing = false, aiError = message) }
            }
        }
    }

    fun openReviewForm() = _uiState.update { it.copy(isReviewFormOpen = true, reviewError = null) }

    fun dismissReviewForm() = _uiState.update { it.copy(isReviewFormOpen = false, reviewError = null) }

    fun submitReview(stars: Int, text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingReview = true, reviewError = null) }
            spotRepository.addReview(spotId, stars, text)
                .onSuccess { _uiState.update { it.copy(isSubmittingReview = false, isReviewFormOpen = false) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isSubmittingReview = false, reviewError = error.toUserMessage()) }
                }
        }
    }

    fun saveEdit(name: String, description: String, openNow: Boolean) {
        viewModelScope.launch {
            spotRepository.editSpot(spotId, name.trim(), description.trim(), openNow)
        }
    }
}
