package com.google.android.gms.example.rewardedvideoexample

import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.example.rewardedvideoexample.ui.theme.RewardedVideoExampleTheme

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
private const val MAIN_TAG = "RewardedVideoExample"

class MainActivity : ComponentActivity() {

  private var mRewardedAd: RewardedAd? = null
  private var isLoading = false

  // UI State
  private var coinCount by mutableStateOf(0)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    MobileAds.initialize(this) {}
    loadRewardedAd()

    setContent {
      RewardedVideoExampleTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          MainScreen(
            coinCount = coinCount,
            onWatchAd = { showRewardedVideo() }
          )
        }
      }
    }
  }

  private fun loadRewardedAd() {
    if (mRewardedAd != null || isLoading) {
      return
    }

    isLoading = true
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
      this,
      AD_UNIT_ID,
      adRequest,
      object : RewardedAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          Log.d(MAIN_TAG, adError.message)
          mRewardedAd = null
          isLoading = false
        }

        override fun onAdLoaded(rewardedAd: RewardedAd) {
          Log.d(MAIN_TAG, "Ad was loaded.")
          mRewardedAd = rewardedAd
          isLoading = false
        }
      }
    )
  }

  private fun showRewardedVideo() {
    if (mRewardedAd != null) {
      mRewardedAd?.show(this) { rewardItem ->
        val rewardAmount = rewardItem.amount
        coinCount += 50 // Fixed reward for demo
        Log.d(MAIN_TAG, "User earned the reward.")
        mRewardedAd = null
        loadRewardedAd() // Preload next ad
      }
    } else {
      Log.d(MAIN_TAG, "The rewarded ad wasn't ready yet.")
      loadRewardedAd()
    }
  }
}

@Composable
fun MainScreen(
  coinCount: Int,
  onWatchAd: () -> Unit
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
        text = "Coin Collector",
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 32.dp)
      )

      Text(
        text = "Coins: $coinCount",
        style = MaterialTheme.typography.displayMedium,
        modifier = Modifier.padding(bottom = 48.dp)
      )

      Button(onClick = onWatchAd) {
        Text(text = "Watch Ad for 50 Coins")
      }
    }
  }
}
