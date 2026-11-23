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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.unieats.app.R
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
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spot.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            painter = painterResource(if (isFavourite) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                StatusLabel(spot.category.label)
                StatusLabel(if (spot.openNow) "Open now" else "Closed")
            }
            Text(
                "★ ${spot.ratingText()} / 5 · ${spot.priceText()}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics {
                    contentDescription = "Rated ${spot.ratingText()} out of 5, price level ${spot.priceLevel}"
                }
            )
            Text(spot.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun StatusLabel(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
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
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "★".repeat(review.stars),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics {
                    contentDescription = "${review.stars} out of 5 stars"
                }
            )
            Text(review.author, style = MaterialTheme.typography.labelLarge)
        }
        Text(review.text, style = MaterialTheme.typography.bodyMedium)
    }
}
