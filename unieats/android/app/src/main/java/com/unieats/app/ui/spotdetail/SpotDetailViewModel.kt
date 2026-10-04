package com.unieats.app.ui.spotdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.SpotRepository
import com.unieats.app.ui.navigation.SpotDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpotDetailUiState(
    val spot: Spot? = null,
    val reviews: List<Review> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class SpotDetailViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val spotId: String = savedStateHandle.toRoute<SpotDetail>().spotId

    private val _uiState = MutableStateFlow(SpotDetailUiState())
    val uiState: StateFlow<SpotDetailUiState> = _uiState.asStateFlow()

    init {
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
}
