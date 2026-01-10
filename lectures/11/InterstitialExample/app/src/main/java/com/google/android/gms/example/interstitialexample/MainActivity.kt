package com.google.android.gms.example.interstitialexample

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.example.interstitialexample.ui.theme.InterstitialExampleTheme

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
private const val MAIN_TAG = "InterstitialExample"

class MainActivity : ComponentActivity() {

  private var mInterstitialAd: InterstitialAd? = null

  // UI State
  private var gameLevel by mutableStateOf(1)
  private var clicks by mutableStateOf(0)
  private val targetClicks = 5

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    MobileAds.initialize(this) {}
    loadAd()

    setContent {
      InterstitialExampleTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainScreen(
            gameLevel = gameLevel,
            clicks = clicks,
            targetClicks = targetClicks,
            onButtonClick = { onMainButtonClick() }
          )
        }
      }
    }
  }

  private fun onMainButtonClick() {
    if (clicks >= targetClicks) {
      // Level Completed - Clicked "Next Level"
      showInterstitial()
    } else {
      // Game in progress - Increment clicks
      clicks++
    }
  }

  private fun startNextLevel() {
    gameLevel++
    clicks = 0
    loadAd()
  }

  private fun showInterstitial() {
    if (mInterstitialAd != null) {
      mInterstitialAd?.show(this)
    } else {
      Log.d(MAIN_TAG, "The interstitial ad wasn't ready yet.")
      startNextLevel()
    }
  }

  private fun loadAd() {
    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      this,
      AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.d(MAIN_TAG, "Ad failed to load: ${adError.message}")
          mInterstitialAd = null
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
          Log.d(MAIN_TAG, "Ad was loaded.")
          mInterstitialAd = interstitialAd

          mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
              Log.d(MAIN_TAG, "Ad was dismissed.")
              mInterstitialAd = null
              startNextLevel()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
              Log.d(MAIN_TAG, "Ad failed to show.")
              mInterstitialAd = null
              startNextLevel()
            }

            override fun onAdShowedFullScreenContent() {
              Log.d(MAIN_TAG, "Ad showed fullscreen content.")
            }
          }
        }
      }
    )
  }
}

@Composable
fun MainScreen(
  gameLevel: Int,
  clicks: Int,
  targetClicks: Int,
  onButtonClick: () -> Unit
) {
  Scaffold { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(16.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Level $gameLevel",
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(bottom = 24.dp)
      )

      if (clicks >= targetClicks) {
        Text(
          text = "Level Complete!",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onButtonClick) {
          Text(text = "Next Level")
        }
      } else {
        Text(
          text = "Clicks: $clicks / $targetClicks",
          style = MaterialTheme.typography.displayMedium,
          modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onButtonClick) {
          Text(text = "Click Me!")
        }
      }
    }
  }
}
