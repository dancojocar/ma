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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList(),
    val isFavourite: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
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
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    init {
        favourites.favouriteIds
            .onEach { ids -> _uiState.update { it.copy(isFavourite = spotId in ids) } }
            .launchIn(viewModelScope)
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val spot = async { spotRepository.fetchSpot(spotId) }
            val reviews = async { spotRepository.fetchReviews(spotId) }
            val spotResult = spot.await()
            val reviewsResult = reviews.await()
            val error = spotResult.exceptionOrNull() ?: reviewsResult.exceptionOrNull()
            _uiState.update {
                it.copy(
                    spot = spotResult.getOrNull() ?: it.spot,
                    reviews = reviewsResult.getOrNull() ?: it.reviews,
                    isLoading = false,
                    errorMessage = error?.toUserMessage()
                )
            }
        }
    }

    fun toggleFavourite() = favourites.toggle(spotId)
}
