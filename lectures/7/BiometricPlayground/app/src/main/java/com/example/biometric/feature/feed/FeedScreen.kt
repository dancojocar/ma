package com.example.biometric.feature.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.biometric.data.FeedItem
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun FeedScreen(viewModel: FeedViewModel = hiltViewModel()) {
    val screenState = viewModel.feed.collectAsState()

    Surface {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            when (val state = screenState.value) {
                FeedState.Loading -> LoadingContent(modifier = Modifier.align(Alignment.Center))
                is FeedState.Failure -> ErrorContent(
                    message = state.message,
                    modifier = Modifier.align(Alignment.Center)
                )
                is FeedState.Success -> SuccessContent(
                    feed = state.feed,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun LoadingContent(modifier: Modifier) {
    CircularProgressIndicator(modifier)
}

@Composable
fun ErrorContent(message: String?, modifier: Modifier) {
    Text(text = message.orEmpty(), modifier)
}

@Composable
fun SuccessContent(
    feed: List<FeedItem>,
    modifier: Modifier
) {
    LazyColumn(modifier = modifier) {
        items(feed) { item ->
            Column {
                Text(item.title, modifier = Modifier.padding(8.dp))
                Text(item.description, modifier = Modifier.padding(8.dp))
            }
        }
    }
}