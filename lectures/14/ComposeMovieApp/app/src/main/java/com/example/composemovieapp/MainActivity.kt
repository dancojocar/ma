package com.example.composemovieapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.composemovieapp.navigation.MovieNavigation
import com.example.composemovieapp.ui.theme.ComposeMovieAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // Enable edge-to-edge display for modern Android UI
    enableEdgeToEdge()

    setContent {
      ComposeMovieAppTheme {
        // A surface container that respects display cutouts
        Surface(
          color = MaterialTheme.colors.background,
          modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
          MovieApp()
        }
      }
    }
  }
}

@Composable
fun MovieApp() {
  MovieNavigation()
}
