package com.example.composemovieapp.movies

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.composemovieapp.components.CircularIndeterminateProgressBar
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.viewmodel.MoviesUiState
import com.example.composemovieapp.movies.viewmodel.MoviesViewModel

@Composable
fun MoviesScreen(
  viewModel: MoviesViewModel = hiltViewModel(),
  onMovieClick: (String) -> Unit
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  ShowError(uiState)

  LazyColumn {
    items(uiState.movies) { item ->
      SingleMovieItem(
        movie = item,
        onMovieClick = onMovieClick
      )
    }
  }
  CircularIndeterminateProgressBar(isDisplayed = uiState.loading)

}

@Composable
private fun ShowError(uiState: MoviesUiState) {
  if (uiState.error) {
    Column(
      modifier = Modifier
        .padding(30.dp)
    ) {
      Text(
        text = "Unable to show the movies!",
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center,
        style = typography.h5,
      )
      Text(
        text = uiState.errorMessage,
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center,
        style = typography.h6,
      )
    }
  }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SingleMovieItem(
  movie: Movie,
  onMovieClick: (String) -> Unit
) {
  Card(
    modifier = Modifier
      .padding(8.dp)
      .fillMaxWidth()
      .clickable { // handling onMovieClick
        onMovieClick(movie.name)
      },
    elevation = 8.dp,
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        modifier = Modifier.size(80.dp),
        painter = rememberImagePainter(
          movie.imageUrl
        ),
        contentDescription = null
      )
      Text(
        text = movie.name,
        fontSize = 24.sp
      )
    }
  }
}