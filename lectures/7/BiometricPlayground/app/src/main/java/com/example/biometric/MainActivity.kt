package com.example.biometric

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import com.example.biometric.feature.NavGraphs
import com.example.composeplayground.ui.theme.CryptoTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CryptoApp()
        }
    }
}

@Composable
private fun CryptoApp() {
    CryptoTheme {
        DestinationsNavHost(navGraph = NavGraphs.root)
    }
}