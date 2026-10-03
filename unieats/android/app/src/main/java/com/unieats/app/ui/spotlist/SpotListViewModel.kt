package com.unieats.app.ui.spotlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unieats.app.cloud.RemoteConfig
import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Spot
import com.unieats.app.data.remote.toUserMessage
import com.unieats.app.data.repository.SpotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpotListUiState(
    val spots: List<Spot> = emptyList(),
    val searchQuery: String = "",
    val categoryFilter: Category? = null,
    val favouriteIds: Set<String> = emptySet(),
    val pendingSpotIds: Set<String> = emptySet(),
    val pendingChanges: Int = 0,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val currentPage: Int = 0,
    val hasNextPage: Boolean = false,
    val isLive: Boolean = false,
    val showNewRatingUi: Boolean = false
)

private data class Background(
    val pendingSpotIds: Set<String>,
    val pendingChanges: Int,
    val isLive: Boolean,
    val showNewRatingUi: Boolean
)

private const val SEARCH_DEBOUNCE_MS = 300L

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SpotListViewModel @Inject constructor(
    private val spotRepository: SpotRepository,
    remoteConfig: RemoteConfig
) : ViewModel() {

    /** Everything the user controls plus load status; the rows themselves come from Room. */
    private val _uiState = MutableStateFlow(SpotListUiState())

    private val spotsFromDb = _uiState
        .map { it.searchQuery to it.categoryFilter }
        .distinctUntilChanged()
        .flatMapLatest { (query, category) -> spotRepository.observeSpots(query, category) }

    private val background = combine(
        spotRepository.observePendingSpotIds(),
        spotRepository.observePendingChanges(),
        spotRepository.liveConnection,
        remoteConfig.flags
    ) { pendingIds, pendingChanges, live, flags ->
        Background(pendingIds, pendingChanges, live, flags.showNewRatingUi)
    }

    val uiState: StateFlow<SpotListUiState> = combine(_uiState, spotsFromDb, background) { state, spots, bg ->
        state.copy(
            spots = spots,
            pendingSpotIds = bg.pendingSpotIds,
            pendingChanges = bg.pendingChanges,
            isLive = bg.isLive,
            showNewRatingUi = bg.showNewRatingUi
        )
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
            spotRepository.refreshSpots(nextPage, state.searchQuery, state.categoryFilter)
                .onSuccess { hasNext ->
                    _uiState.update { it.copy(currentPage = nextPage, hasNextPage = hasNext, isLoadingMore = false) }
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
            spotRepository.refreshSpots(1, state.searchQuery, state.categoryFilter)
                .onSuccess { hasNext ->
                    _uiState.update {
                        it.copy(currentPage = 1, hasNextPage = hasNext, isLoading = false, isRefreshing = false)
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
