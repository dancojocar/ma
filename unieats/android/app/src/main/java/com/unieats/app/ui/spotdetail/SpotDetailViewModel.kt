package com.unieats.app.ui.spotdetail

import androidx.lifecycle.ViewModel
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.repository.SpotRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList()
)

@HiltViewModel(assistedFactory = SpotDetailViewModel.Factory::class)
class SpotDetailViewModel @AssistedInject constructor(
    spotRepository: SpotRepository,
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
}
