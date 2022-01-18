package com.example.composemovieapp.movies

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.composemovieapp.components.CircularIndeterminateProgressBar
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.viewmodel.MoviesViewModel

@Composable
fun MoviesScreen(
  viewModel: MoviesViewModel = hiltViewModel(),
  onMovieClick: (String) -> Unit
) {
  val loading by remember { viewModel.loading }
  val listOfMovies by remember { viewModel.listOfMovies }

  LazyColumn {
    items(listOfMovies) { item ->
      SingleMovieItem(
        movie = item,
        onMovieClick = onMovieClick
      )
    }
  }
  CircularIndeterminateProgressBar(isDisplayed = loading)
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