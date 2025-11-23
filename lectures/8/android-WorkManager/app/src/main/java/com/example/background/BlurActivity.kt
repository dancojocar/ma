package com.example.background

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import com.example.background.ui.BlurScreen

class BlurActivity : ComponentActivity() {

  private val viewModel: BlurViewModel by viewModels { BlurViewModelFactory(application) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      BlurScreen(viewModel)
    }
  }
}
