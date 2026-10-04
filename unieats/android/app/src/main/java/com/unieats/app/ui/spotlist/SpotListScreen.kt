package com.unieats.app.ui.spotlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.data.model.Category

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
    onSpotClick: (String) -> Unit
) {
    var sortByRating by rememberSaveable { mutableStateOf(false) }
    val spots = if (sortByRating) state.spots.sortedByDescending { it.rating } else state.spots

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UniEats") },
                actions = {
                    TextButton(onClick = { sortByRating = !sortByRating }) {
                        Text(if (sortByRating) "Sort: rating" else "Sort: default")
                    }
                }
            )
        }
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
            if (spots.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No spots match your search.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // The stable key lets Compose move rows instead of rebinding them; remove it and
                    // the sort toggle flashes instead of animating.
                    items(spots, key = { it.id }) { spot ->
                        SpotCard(
                            spot = spot,
                            isFavourite = spot.id in state.favouriteIds,
                            onClick = { onSpotClick(spot.id) },
                            onToggleFavourite = { onToggleFavourite(spot.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}
