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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.R
import com.unieats.app.data.model.Category
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
    onLogout: () -> Unit,
    biometricAvailable: Boolean,
    biometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit,
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
        onLogout = onLogout,
        biometricAvailable = biometricAvailable,
        biometricEnabled = biometricEnabled,
        onToggleBiometric = onToggleBiometric
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
    onLogout: () -> Unit,
    biometricAvailable: Boolean,
    biometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit
) {
    var sortByRating by rememberSaveable { mutableStateOf(false) }
    val spots = remember(state.spots, sortByRating) {
        if (sortByRating) state.spots.sortedByDescending { it.rating } else state.spots
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    if (state.spots.isNotEmpty() && state.errorMessage != null) {
        LaunchedEffect(state.errorMessage) {
            val result = snackbarHostState.showSnackbar(state.errorMessage, actionLabel = "Retry")
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
                title = { Text("UniEats") },
                actions = {
                    LiveBadge(isLive = state.isLive)
                    IconButton(onClick = onNearbyClick) {
                        Icon(painterResource(R.drawable.ic_location_on), contentDescription = "Spots near me")
                    }
                    OverflowMenu(
                        sortByRating = sortByRating,
                        onToggleSort = { sortByRating = !sortByRating },
                        onLogout = onLogout,
                        // Also shown when biometrics were removed meanwhile, so it can be turned off.
                        showBiometricToggle = biometricAvailable || biometricEnabled,
                        biometricEnabled = biometricEnabled,
                        onToggleBiometric = onToggleBiometric
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
                    spots.isEmpty() -> ScrollableState { MessageView("No spots match your search.") }
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
    onLogout: () -> Unit,
    showBiometricToggle: Boolean,
    biometricEnabled: Boolean,
    onToggleBiometric: (Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = "More options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(if (sortByRating) "Default order" else "Sort by rating") },
                onClick = {
                    expanded = false
                    onToggleSort()
                }
            )
            if (showBiometricToggle) {
                DropdownMenuItem(
                    text = { Text("Unlock with biometrics") },
                    trailingIcon = { Switch(checked = biometricEnabled, onCheckedChange = null) },
                    onClick = {
                        expanded = false
                        onToggleBiometric(!biometricEnabled)
                    },
                    modifier = Modifier.semantics { stateDescription = if (biometricEnabled) "On" else "Off" }
                )
            }
            DropdownMenuItem(
                text = { Text("Log out") },
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
        modifier = Modifier.clearAndSetSemantics {
            contentDescription = if (isLive) "Live updates on" else "Offline"
        },
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
