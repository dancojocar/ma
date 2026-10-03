package com.unieats.app.ui.spotlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Spot
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpotListUiState(
    val spots: List<Spot> = emptyList(),
    val searchQuery: String = "",
    val categoryFilter: Category? = null,
    val favouriteIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val hasNextPage: Boolean = false
)

private const val SEARCH_DEBOUNCE_MS = 300L

@HiltViewModel
class SpotListViewModel @Inject constructor(
    private val spotRepository: SpotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotListUiState())
    val uiState: StateFlow<SpotListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null
    private var retryFailedLoad: () -> Unit = { loadFirstPage() }

    init {
        loadFirstPage()
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadFirstPage(debounceMs = SEARCH_DEBOUNCE_MS)
    }

    fun onCategorySelected(category: Category?) {
        _uiState.update { it.copy(categoryFilter = category) }
        loadFirstPage()
    }

    fun toggleFavourite(spotId: String) {
        _uiState.update { state ->
            val ids = state.favouriteIds
            state.copy(favouriteIds = if (spotId in ids) ids - spotId else ids + spotId)
        }
    }

    fun refresh() = loadFirstPage(refreshing = true)

    fun retry() = retryFailedLoad()

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.hasNextPage || loadJob?.isActive == true) return
        val nextPage = state.currentPage + 1
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            spotRepository.fetchSpots(nextPage, state.searchQuery, state.categoryFilter)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            spots = (it.spots + page.spots).distinctBy(Spot::id),
                            currentPage = nextPage,
                            hasNextPage = page.hasNextPage,
                            isLoadingMore = false
                        )
                    }
                }
                .onFailure { error ->
                    retryFailedLoad = ::loadNextPage
                    _uiState.update { it.copy(isLoadingMore = false, errorMessage = error.toUserMessage()) }
                }
        }
    }

    private fun loadFirstPage(debounceMs: Long = 0, refreshing: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            delay(debounceMs)
            _uiState.update { it.copy(isLoading = !refreshing, isRefreshing = refreshing, errorMessage = null) }
            val state = _uiState.value
            spotRepository.fetchSpots(1, state.searchQuery, state.categoryFilter)
                .onSuccess { page ->
                    _uiState.update {
                        it.copy(
                            spots = page.spots,
                            currentPage = 1,
                            hasNextPage = page.hasNextPage,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .onFailure { error ->
                    retryFailedLoad = { loadFirstPage() }
                    _uiState.update {
                        it.copy(isLoading = false, isRefreshing = false, errorMessage = error.toUserMessage())
                    }
                }
        }
    }
}
