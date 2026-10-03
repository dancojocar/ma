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
import androidx.compose.material3.OutlinedButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.shared.Review
import com.unieats.shared.Spot
import com.unieats.app.ui.components.ErrorView
import com.unieats.app.ui.components.LoadingView
import com.unieats.app.ui.components.MessageView
import com.unieats.app.ui.components.SpotPhoto
import com.unieats.app.ui.components.priceText
import com.unieats.app.ui.components.ratingText

@Composable
fun SpotDetailScreen(
    onBack: () -> Unit,
    viewModel: SpotDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SpotDetailContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onSaveEdit = viewModel::saveEdit,
        onAddReview = viewModel::openReviewForm,
        onDismissReview = viewModel::dismissReviewForm,
        onSubmitReview = viewModel::submitReview
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailContent(
    state: SpotDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSaveEdit: (name: String, description: String, openNow: Boolean) -> Unit,
    onAddReview: () -> Unit,
    onDismissReview: () -> Unit,
    onSubmitReview: (stars: Int, text: String) -> Unit
) {
    val spot = state.spot
    var editing by rememberSaveable { mutableStateOf(false) }

    if (editing && spot != null) {
        EditSpotDialog(
            spot = spot,
            onDismiss = { editing = false },
            onSave = { name, description, openNow ->
                onSaveEdit(name, description, openNow)
                editing = false
            }
        )
    }

    if (state.isReviewFormOpen) {
        AddReviewDialog(
            isSubmitting = state.isSubmittingReview,
            errorMessage = state.reviewError,
            onDismiss = onDismissReview,
            onSubmit = onSubmitReview
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(spot?.name ?: "Spot") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            spot == null && state.isLoading -> LoadingView(contentModifier)
            spot == null && state.errorMessage != null -> ErrorView(state.errorMessage, onRetry, contentModifier)
            spot == null -> MessageView("Spot not found", contentModifier)
            else -> LazyColumn(modifier = contentModifier.fillMaxSize()) {
                if (state.errorMessage != null) {
                    item { StaleDataNotice(state.errorMessage, onRetry) }
                }
                item { SpotHeader(spot, state.isPendingSync, onEdit = { editing = true }) }
                item { ReviewsHeader(state.reviews.size, onAddReview) }
                items(state.reviews, key = { it.id }) { review -> ReviewRow(review) }
            }
        }
    }
}

@Composable
private fun StaleDataNotice(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Showing saved data. $message",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun SpotHeader(spot: Spot, isPendingSync: Boolean, onEdit: () -> Unit) {
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
                if (isPendingSync) AssistChip(onClick = {}, label = { Text("Waiting to sync") })
            }
            Text(
                "★ ${spot.ratingText()} / 5 · ${spot.priceText()}",
                style = MaterialTheme.typography.bodyLarge
            )
            AboutCard(spot)
            OutlinedButton(onClick = onEdit) { Text("Edit spot") }
        }
    }
}

@Composable
private fun ReviewsHeader(count: Int, onAddReview: () -> Unit) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reviews ($count)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAddReview) { Text("Add review") }
        }
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
