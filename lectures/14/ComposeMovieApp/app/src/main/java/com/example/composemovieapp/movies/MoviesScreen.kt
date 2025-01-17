package com.example.composemovieapp.movies

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.example.composemovieapp.components.CircularIndeterminateProgressBar
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.viewmodel.MoviesUiState
import com.example.composemovieapp.movies.viewmodel.MoviesViewModel

@Composable
fun MoviesScreen(
    viewModel: MoviesViewModel,
    onMovieClick: (String) -> Unit,
    onAddMovieClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Load initial data if needed
    LaunchedEffect(Unit) {
        viewModel.loadInitialDataIfNeeded()
    }
    
    // Track state changes
    LaunchedEffect(uiState) {
        Log.d("MoviesScreen", "Movies list size: ${uiState.movies.size} Error: ${uiState.error}")
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMovieClick
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Movie")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            if (uiState.movies.isEmpty() && !uiState.loading && !uiState.error) {
                Text(
                    text = "No movies available",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn {
                    items(
                        items = uiState.movies,
                        key = { movie -> movie.name }
                    ) { movie ->
                        SingleMovieItem(
                            movie = movie,
                            onMovieClick = onMovieClick
                        )
                    }
                }
            }
            
            ShowError(uiState)
            CircularIndeterminateProgressBar(isDisplayed = uiState.loading)
        }
    }
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
            .clickable { 
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
                painter = rememberAsyncImagePainter(
                    movie.imageUrl
                ),
                contentDescription = null
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = movie.name,
                    fontSize = 24.sp,
                    style = typography.h6
                )
                Text(
                    text = movie.category,
                    style = typography.subtitle1,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}