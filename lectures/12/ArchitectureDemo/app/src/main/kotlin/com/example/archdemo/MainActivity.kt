package com.example.archdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.archdemo.ui.ArchDemoApp
import com.example.archdemo.ui.theme.ArchitectureDemoTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.archdemo.ui.ArchDemoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ArchitectureDemoTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val viewModel: ArchDemoViewModel = viewModel()
                    ArchDemoApp(viewModel = viewModel)
                }
            }
        }
    }
}