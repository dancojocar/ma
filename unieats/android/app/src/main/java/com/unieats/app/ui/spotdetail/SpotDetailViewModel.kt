package com.unieats.app.ui.spotdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.FavouritesRepository
import com.unieats.app.data.repository.SpotRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val isLive: Boolean = false
)

@HiltViewModel(assistedFactory = SpotDetailViewModel.Factory::class)
class SpotDetailViewModel @AssistedInject constructor(
    private val spotRepository: SpotRepository,
    private val favourites: FavouritesRepository,
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

    fun saveEdit(name: String, description: String, openNow: Boolean) {
        viewModelScope.launch {
            spotRepository.editSpot(spotId, name.trim(), description.trim(), openNow)
        }
    }
}
