package com.example.composemovieapp.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.viewmodel.MoviesViewModel

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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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