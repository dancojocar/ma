package com.example.biometric

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.fragment.app.FragmentActivity
import com.ramcosta.composedestinations.generated.NavGraphs
import com.example.composeplayground.ui.theme.CryptoTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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