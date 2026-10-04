package com.unieats.app.ui.nearby

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.model.Spot
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.SpotRepository
import com.unieats.app.location.LatLng
import com.unieats.app.location.LocationService
import com.unieats.app.location.NEARBY_RADIUS_METERS
import com.unieats.app.location.distanceMeters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NearbySpot(val spot: Spot, val distanceMeters: Double)

data class NearbyUiState(
    val hasPermission: Boolean = false,
    val location: LatLng? = null,
    val spots: List<NearbySpot> = emptyList(),
    val errorMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    private val locationService: LocationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyUiState(hasPermission = locationService.hasPermission()))

    @SuppressLint("MissingPermission")
    private val location: Flow<LatLng?> = _uiState
        .map { it.hasPermission }
        .flatMapLatest { granted -> if (granted) locationService.locationUpdates().onStart<LatLng?> { emit(null) } else flowOf(null) }

    // WhileSubscribed() with no grace period: location updates stop as soon as the screen stops
    // collecting (it leaves the screen or the app goes to the background).
    val uiState: StateFlow<NearbyUiState> = combine(
        _uiState,
        location,
        spotRepository.observeSpots(query = "", category = null)
    ) { state, here, spots ->
        state.copy(location = here, spots = here?.let { nearby(spots, it) }.orEmpty())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _uiState.value)

    init {
        viewModelScope.launch {
            spotRepository.refreshAllSpots().onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.toUserMessage()) }
            }
        }
    }

    fun onPermissionResult() = _uiState.update { it.copy(hasPermission = locationService.hasPermission()) }

    private fun nearby(spots: List<Spot>, here: LatLng): List<NearbySpot> =
        spots.map { NearbySpot(it, distanceMeters(here, LatLng(it.lat, it.lng))) }
            .filter { it.distanceMeters <= NEARBY_RADIUS_METERS }
            .sortedBy { it.distanceMeters }
}
