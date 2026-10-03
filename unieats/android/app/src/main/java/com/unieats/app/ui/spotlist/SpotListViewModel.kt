package com.unieats.app.ui.spotlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Spot
import com.unieats.app.data.remote.LiveEvent
import com.unieats.app.data.remote.LiveUpdates
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
    val hasNextPage: Boolean = false,
    val isLive: Boolean = false
)

fun Spot.matches(query: String, category: Category?): Boolean {
    val q = query.trim()
    val textMatches = name.contains(q, ignoreCase = true) || description.contains(q, ignoreCase = true)
    return textMatches && (category == null || this.category == category)
}

private const val SEARCH_DEBOUNCE_MS = 300L

@HiltViewModel
class SpotListViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    liveUpdates: LiveUpdates
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpotListUiState())

    // The WebSocket is part of the upstream: it is open only while the screen collects uiState
    // (plus 5 s, so a rotation does not reconnect).
    private val liveConnection: Flow<Boolean> = liveUpdates.events()
        .onEach(::applyLiveEvent)
        .map { it != LiveEvent.Disconnected }
        .onStart { emit(false) }
        .distinctUntilChanged()

    val uiState: StateFlow<SpotListUiState> = combine(_uiState, liveConnection) { state, live ->
        state.copy(isLive = live)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), _uiState.value)

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

    private fun applyLiveEvent(event: LiveEvent) {
        _uiState.update { state ->
            when (event) {
                is LiveEvent.SpotUpdated -> state.copy(
                    spots = state.spots.map { if (it.id == event.spot.id) event.spot else it }
                )
                is LiveEvent.SpotCreated ->
                    if (event.spot.matches(state.searchQuery, state.categoryFilter)) {
                        state.copy(spots = listOf(event.spot) + state.spots.filterNot { it.id == event.spot.id })
                    } else {
                        state
                    }
                is LiveEvent.SpotDeleted -> state.copy(spots = state.spots.filterNot { it.id == event.id })
                LiveEvent.Connected, LiveEvent.Disconnected -> state
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
