package com.unieats.app.ui.spotlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Spot
import com.unieats.app.data.repository.FavouritesRepository
import com.unieats.app.data.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SpotListUiState(
    val spots: List<Spot> = emptyList(),
    val searchQuery: String = "",
    val categoryFilter: Category? = null,
    val favouriteIds: Set<String> = emptySet()
)

fun List<Spot>.filterBy(query: String, category: Category?): List<Spot> = filter { spot ->
    spot.name.contains(query.trim(), ignoreCase = true) &&
        (category == null || spot.category == category)
}

@HiltViewModel
class SpotListViewModel @Inject constructor(
    spotRepository: SpotRepository,
    private val favourites: FavouritesRepository
) : ViewModel() {

    private val allSpots = spotRepository.getSpots()

    private val _uiState = MutableStateFlow(SpotListUiState(spots = allSpots))
    val uiState: StateFlow<SpotListUiState> = _uiState.asStateFlow()

    init {
        favourites.favouriteIds
            .onEach { ids -> _uiState.update { it.copy(favouriteIds = ids) } }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, spots = allSpots.filterBy(query, it.categoryFilter)) }
    }

    fun onCategorySelected(category: Category?) {
        _uiState.update { it.copy(categoryFilter = category, spots = allSpots.filterBy(it.searchQuery, category)) }
    }

    fun toggleFavourite(spotId: String) = favourites.toggle(spotId)
}
