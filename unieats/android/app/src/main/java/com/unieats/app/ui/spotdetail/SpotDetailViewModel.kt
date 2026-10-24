package com.unieats.app.ui.spotdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.repository.FavouritesRepository
import com.unieats.app.data.repository.SpotRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList(),
    val isFavourite: Boolean = false
)

@HiltViewModel(assistedFactory = SpotDetailViewModel.Factory::class)
class SpotDetailViewModel @AssistedInject constructor(
    spotRepository: SpotRepository,
    private val favourites: FavouritesRepository,
    @Assisted private val spotId: String
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(spotId: String): SpotDetailViewModel
    }

    private val _uiState = MutableStateFlow(
        SpotDetailUiState(
            spot = spotRepository.getSpot(spotId),
            reviews = spotRepository.getReviews(spotId)
        )
    )
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    init {
        favourites.favouriteIds
            .onEach { ids -> _uiState.update { it.copy(isFavourite = spotId in ids) } }
            .launchIn(viewModelScope)
    }

    fun toggleFavourite() = favourites.toggle(spotId)
}
