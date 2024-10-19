package com.example.composemovieapp.details

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun MovieDetailsScreen(
  selectedMovie: String,
  onButtonClick: () -> Unit
) {
  Column {
    Button(onClick = onButtonClick) {
      Text("Go back")
    }
    Text("Details for $selectedMovie are not implemented yet!")
  }
}