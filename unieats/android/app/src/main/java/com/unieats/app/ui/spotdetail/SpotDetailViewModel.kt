package com.unieats.app.ui.spotdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.repository.SpotRepository
import com.unieats.app.ui.navigation.SpotDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList()
)

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    spotRepository: SpotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spotId: String = savedStateHandle.toRoute<SpotDetail>().spotId

    private val _uiState = MutableStateFlow(
        SpotDetailUiState(
            spot = spotRepository.getSpot(spotId),
            reviews = spotRepository.getReviews(spotId)
        )
    )
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()
}
