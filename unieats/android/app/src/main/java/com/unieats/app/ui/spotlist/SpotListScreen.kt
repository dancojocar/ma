package com.unieats.app.ui.spotlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unieats.app.data.model.Spot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotListScreen(
    spots: List<Spot>,
    onSpotClick: (String) -> Unit
) {
    var sortByRating by rememberSaveable { mutableStateOf(false) }
    val sorted = if (sortByRating) spots.sortedByDescending { it.rating } else spots

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
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // The stable key lets Compose move rows instead of rebinding them; remove it and the
            // sort toggle flashes instead of animating.
            items(sorted, key = { it.id }) { spot ->
                SpotCard(
                    spot = spot,
                    onClick = { onSpotClick(spot.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
