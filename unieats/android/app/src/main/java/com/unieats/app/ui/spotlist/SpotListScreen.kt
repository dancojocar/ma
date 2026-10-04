package com.unieats.app.ui.spotlist

import android.Manifest
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.R
import com.unieats.shared.Category
import com.unieats.app.ui.components.ErrorView
import com.unieats.app.ui.components.LoadingView
import com.unieats.app.ui.components.MessageView
import com.unieats.app.ui.permissions.LOCAL_NETWORK_PERMISSIONS
import com.unieats.app.ui.permissions.NOTIFICATION_PERMISSIONS
import com.unieats.app.ui.permissions.RequestPermissionsOnce

@Composable
fun SpotListScreen(
    onSpotClick: (String) -> Unit,
    onNearbyClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
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
        onErrorShown = viewModel::errorShown,
        onSpotClick = onSpotClick,
        onNearbyClick = onNearbyClick,
        onSettingsClick = onSettingsClick,
        onLogout = onLogout
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
    onErrorShown: () -> Unit,
    onSpotClick: (String) -> Unit,
    onNearbyClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    var sortByRating by rememberSaveable { mutableStateOf(false) }
    val spots = remember(state.spots, sortByRating) {
        if (sortByRating) state.spots.sortedByDescending { it.rating } else state.spots
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val retryLabel = stringResource(R.string.action_retry)

    if (state.spots.isNotEmpty() && state.errorMessage != null) {
        LaunchedEffect(state.errorMessage) {
            val result = snackbarHostState.showSnackbar(state.errorMessage, actionLabel = retryLabel)
            if (result == SnackbarResult.ActionPerformed) onRetry() else onErrorShown()
        }
    }
    InfiniteScrollEffect(listState, state, onLoadMore)
    // A load started before ACCESS_LOCAL_NETWORK was granted has already failed; reload once it is.
    RequestPermissionsOnce(LOCAL_NETWORK_PERMISSIONS + NOTIFICATION_PERMISSIONS) { granted ->
        if (granted[Manifest.permission.ACCESS_LOCAL_NETWORK] == true) onRefresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    LiveBadge(isLive = state.isLive)
                    IconButton(onClick = onNearbyClick) {
                        Icon(painterResource(R.drawable.ic_location_on), contentDescription = stringResource(R.string.list_nearby))
                    }
                    OverflowMenu(
                        sortByRating = sortByRating,
                        onToggleSort = { sortByRating = !sortByRating },
                        onSettingsClick = onSettingsClick,
                        onLogout = onLogout
                    )
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
                    state.isLoading && state.spots.isEmpty() -> ScrollableState { LoadingView() }
                    state.errorMessage != null && state.spots.isEmpty() ->
                        ScrollableState { ErrorView(message = state.errorMessage, onRetry = onRetry) }
                    spots.isEmpty() -> ScrollableState { MessageView(stringResource(R.string.list_empty)) }
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
                                showNewRatingUi = state.showNewRatingUi,
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

// PullToRefreshBox only reacts to a nested scroll, so the non-list states must scroll too.
@Composable
private fun ScrollableState(content: @Composable () -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { Box(Modifier.fillParentMaxSize()) { content() } }
    }
}

@Composable
private fun OverflowMenu(
    sortByRating: Boolean,
    onToggleSort: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.list_more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(if (sortByRating) R.string.list_sort_default else R.string.list_sort_by_rating)) },
                onClick = {
                    expanded = false
                    onToggleSort()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.list_settings)) },
                onClick = {
                    expanded = false
                    onSettingsClick()
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.list_log_out)) },
                onClick = {
                    expanded = false
                    onLogout()
                }
            )
        }
    }
}

@Composable
private fun PendingChangesBanner(count: Int) {
    Text(
        text = pluralStringResource(R.plurals.list_changes_pending, count, count),
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
    val description = stringResource(if (isLive) R.string.live_updates_on else R.string.live_updates_offline)
    Text(
        text = stringResource(if (isLive) R.string.live_badge_live else R.string.live_badge_offline),
        modifier = Modifier.clearAndSetSemantics { contentDescription = description },
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
