package com.unieats.app.ui.spotdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.shared.Review
import com.unieats.shared.Spot
import com.unieats.app.data.remote.AiDescribeClient
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.EatsRepository
import com.unieats.app.data.repository.FavouritesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList(),
    val isFavourite: Boolean = false,
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

@HiltViewModel(assistedFactory = SpotDetailViewModel.Factory::class)
class SpotDetailViewModel @AssistedInject constructor(
    private val spotRepository: EatsRepository,
    private val favourites: FavouritesRepository,
    private val aiClient: AiDescribeClient,
    @Assisted private val spotId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(spotId: String): SpotDetailViewModel
    }

    private val _uiState = MutableStateFlow(SpotDetailUiState())

    private val spotWithReviews = combine(
        spotRepository.observeSpot(spotId),
        spotRepository.observeReviews(spotId)
    ) { spot, reviews -> spot to reviews }

    val uiState: StateFlow<SpotDetailUiState> = combine(
        _uiState,
        spotWithReviews,
        spotRepository.observePendingSpotIds(),
        favourites.favouriteIds,
        spotRepository.liveConnection
    ) { state, (spot, reviews), pendingIds, favouriteIds, live ->
        state.copy(
            spot = spot,
            reviews = reviews,
            isFavourite = spotId in favouriteIds,
            isPendingSync = spotId in pendingIds,
            isLive = live
        )
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

    fun toggleFavourite() = favourites.toggle(spotId)

    private var describeJob: Job? = null

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
                _uiState.update { it.copy(isDescribing = false, aiError = e.toUserMessage()) }
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
