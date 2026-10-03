package com.unieats.app.ui.spotlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.data.model.Category
import com.unieats.app.ui.components.ErrorView
import com.unieats.app.ui.components.LoadingView
import com.unieats.app.ui.components.MessageView

@Composable
fun SpotListScreen(
    onSpotClick: (String) -> Unit,
    viewModel: SpotListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SpotListContent(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onCategorySelected = viewModel::onCategorySelected,
        onToggleFavourite = viewModel::toggleFavourite,
        onRefresh = viewModel::refresh,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        onSpotClick = onSpotClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotListContent(
    state: SpotListUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (Category?) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onSpotClick: (String) -> Unit
) {
    var sortByRating by rememberSaveable { mutableStateOf(false) }
    val spots = if (sortByRating) state.spots.sortedByDescending { it.rating } else state.spots
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    if (state.spots.isNotEmpty() && state.errorMessage != null) {
        LaunchedEffect(state.errorMessage) {
            val result = snackbarHostState.showSnackbar(state.errorMessage, actionLabel = "Retry")
            if (result == SnackbarResult.ActionPerformed) onRetry()
        }
    }
    InfiniteScrollEffect(listState, state, onLoadMore)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UniEats") },
                actions = {
                    LiveBadge(isLive = state.isLive)
                    TextButton(onClick = { sortByRating = !sortByRating }) {
                        Text(if (sortByRating) "Sort: rating" else "Sort: default")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SpotSearchBar(
                query = state.searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            CategoryFilterRow(selected = state.categoryFilter, onSelect = onCategorySelected)
            if (state.pendingChanges > 0) PendingChangesBanner(state.pendingChanges)
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.spots.isEmpty() -> LoadingView()
                    state.errorMessage != null && state.spots.isEmpty() ->
                        ErrorView(message = state.errorMessage, onRetry = onRetry)
                    spots.isEmpty() -> MessageView("No spots match your search.")
                    else -> LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // The stable key lets Compose move rows instead of rebinding them; remove it
                        // and the sort toggle flashes instead of animating.
                        items(spots, key = { it.id }) { spot ->
                            SpotCard(
                                spot = spot,
                                isFavourite = spot.id in state.favouriteIds,
                                isPending = spot.id in state.pendingSpotIds,
                                onClick = { onSpotClick(spot.id) },
                                onToggleFavourite = { onToggleFavourite(spot.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                        if (state.isLoadingMore || state.isLoading) {
                            item(key = "loading-more") {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingChangesBanner(count: Int) {
    Text(
        text = if (count == 1) "1 change pending" else "$count changes pending",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun LiveBadge(isLive: Boolean) {
    Text(
        text = if (isLive) "● Live" else "○ Offline",
        style = MaterialTheme.typography.labelMedium,
        color = if (isLive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InfiniteScrollEffect(listState: LazyListState, state: SpotListUiState, onLoadMore: () -> Unit) {
    val nearEnd by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(nearEnd, state.hasNextPage, state.spots.size) {
        if (nearEnd && state.hasNextPage && state.errorMessage == null) onLoadMore()
    }
}
