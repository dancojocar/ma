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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unieats.app.R
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
    viewModel: SpotDetailViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SpotDetailContent(
        state = state,
        isFavourite = state.isFavourite,
        onToggleFavourite = viewModel::toggleFavourite,
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
    isFavourite: Boolean,
    onToggleFavourite: () -> Unit,
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
                title = { Text(spot?.name ?: stringResource(R.string.detail_title_fallback)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavourite) {
                        Icon(
                            painter = painterResource(if (isFavourite) R.drawable.ic_favorite else R.drawable.ic_favorite_border),
                            contentDescription = stringResource(if (isFavourite) R.string.action_remove_favourite else R.string.action_add_favourite),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            spot == null && state.isLoading -> LoadingView(contentModifier)
            spot == null && state.errorMessage != null -> ErrorView(state.errorMessage, onRetry, contentModifier)
            spot == null -> MessageView(stringResource(R.string.detail_not_found), contentModifier)
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
            stringResource(R.string.detail_stale_data, message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f)
        )
        TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
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
                StatusLabel(spot.category.label)
                StatusLabel(stringResource(if (spot.openNow) R.string.status_open_now else R.string.status_closed))
                if (isPendingSync) StatusLabel(stringResource(R.string.status_waiting_to_sync))
            }
            val ratingDescription = stringResource(R.string.detail_rating_description, spot.ratingText(), spot.priceLevel)
            Text(
                stringResource(R.string.detail_rating_line, spot.ratingText(), spot.priceText()),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { contentDescription = ratingDescription }
            )
            AboutCard(spot)
            OutlinedButton(onClick = onEdit) { Text(stringResource(R.string.detail_edit_spot)) }
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
                text = pluralStringResource(R.plurals.detail_reviews_header, count, count),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAddReview) { Text(stringResource(R.string.detail_add_review)) }
        }
        if (count == 0) {
            Text(
                stringResource(R.string.detail_no_reviews),
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
            val starsDescription = pluralStringResource(R.plurals.review_stars, review.stars, review.stars)
            Text(
                "★".repeat(review.stars),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.clearAndSetSemantics { contentDescription = starsDescription }
            )
            Text(review.author, style = MaterialTheme.typography.labelLarge)
        }
        Text(review.text, style = MaterialTheme.typography.bodyMedium)
    }
}
