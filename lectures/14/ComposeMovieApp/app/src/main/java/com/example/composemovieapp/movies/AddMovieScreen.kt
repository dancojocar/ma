package com.example.composemovieapp.movies

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.viewmodel.MoviesViewModel
import kotlinx.coroutines.launch

@Composable
fun AddMovieScreen(
  viewModel: MoviesViewModel,
  onNavigateBack: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var imageUrl by remember { mutableStateOf("") }
  var link by remember { mutableStateOf("") }

  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Add New Movie") },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        if (uiState.loading) {
          CircularProgressIndicator(
            modifier = Modifier
              .size(50.dp)
          )
        }
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Movie Name*") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = uiState.fieldErrors.containsKey("name"),
          trailingIcon = {
            if (uiState.fieldErrors.containsKey("name")) {
              Text(
                text = uiState.fieldErrors["name"] ?: "",
                color = MaterialTheme.colors.error
              )
            }
          }
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Category*") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = uiState.fieldErrors.containsKey("category"),
          trailingIcon = {
            if (uiState.fieldErrors.containsKey("category")) {
              Text(
                text = uiState.fieldErrors["category"] ?: "",
                color = MaterialTheme.colors.error
              )
            }
          }
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          maxLines = 4,
          isError = uiState.fieldErrors.containsKey("desc"),
          trailingIcon = {
            if (uiState.fieldErrors.containsKey("desc")) {
              Text(
                text = uiState.fieldErrors["desc"] ?: "",
                color = MaterialTheme.colors.error
              )
            }
          }
        )

        OutlinedTextField(
          value = imageUrl,
          onValueChange = { imageUrl = it },
          label = { Text("Image URL") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = uiState.fieldErrors.containsKey("imageUrl"),
          trailingIcon = {
            if (uiState.fieldErrors.containsKey("imageUrl")) {
              Text(
                text = uiState.fieldErrors["imageUrl"] ?: "",
                color = MaterialTheme.colors.error
              )
            }
          }
        )

        OutlinedTextField(
          value = link,
          onValueChange = { link = it },
          label = { Text("Link") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = uiState.fieldErrors.containsKey("link"),
          trailingIcon = {
            if (uiState.fieldErrors.containsKey("link")) {
              Text(
                text = uiState.fieldErrors["link"] ?: "",
                color = MaterialTheme.colors.error
              )
            }
          }
        )

        if (uiState.error && uiState.fieldErrors.isEmpty()) {
          Text(
            text = uiState.errorMessage,
            color = MaterialTheme.colors.error,
            style = MaterialTheme.typography.caption,
            modifier = Modifier.padding(top = 8.dp)
          )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
          onClick = {
            val movie = Movie(
              name = name,
              category = category,
              desc = description,
              imageUrl = imageUrl.ifBlank { "https://via.placeholder.com/150" },
              link = link
            )
            viewModel.addMovie(
              movie = movie,
              onSuccess = {
                onNavigateBack()
              }
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
        ) {
          if (uiState.loading) {
            CircularProgressIndicator(
              color = MaterialTheme.colors.onPrimary,
              modifier = Modifier.size(24.dp)
            )
          } else {
            Text("Add Movie")
          }
        }
      }

    }
  }
} 