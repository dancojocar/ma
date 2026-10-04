package com.unieats.app.ui.spotdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.ui.components.SpotPhoto
import com.unieats.app.ui.components.priceText
import com.unieats.app.ui.components.ratingText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailScreen(
    spot: Spot,
    reviews: List<Review>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spot.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { SpotHeader(spot) }
            item { ReviewsHeader(reviews.size) }
            items(reviews, key = { it.id }) { review -> ReviewRow(review) }
        }
    }
}

@Composable
private fun SpotHeader(spot: Spot) {
    Column {
        SpotPhoto(
            url = spot.photoUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(spot.name, style = MaterialTheme.typography.headlineSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(onClick = {}, label = { Text(spot.category.label) })
                AssistChip(onClick = {}, label = { Text(if (spot.openNow) "Open now" else "Closed") })
            }
            Text(
                "★ ${spot.ratingText()} / 5 · ${spot.priceText()}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(spot.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReviewsHeader(count: Int) {
    Column {
        HorizontalDivider()
        Text(
            text = "Reviews ($count)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (count == 0) {
            Text(
                "No reviews yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ReviewRow(review: Review) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            "${"★".repeat(review.stars)} ${review.author}",
            style = MaterialTheme.typography.labelLarge
        )
        Text(review.text, style = MaterialTheme.typography.bodyMedium)
    }
}
