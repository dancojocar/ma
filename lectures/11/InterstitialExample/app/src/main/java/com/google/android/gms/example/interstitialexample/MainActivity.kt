package com.google.android.gms.example.interstitialexample

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.example.interstitialexample.databinding.ActivityMainBinding

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  private var mInterstitialAd: InterstitialAd? = null
  private var timerFinished = false
  private var countdownTimer: CountDownTimer? = null
  private var lastRetryTimestamp = 0L // Track last retry time

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    loadAd() // Load the initial ad

    binding.retryButton.setOnClickListener {
      val currentTime = System.currentTimeMillis()
      if (currentTime - lastRetryTimestamp < 10_000) {
        val message = "Retry button is disabled. Please wait 10 seconds before trying again."
        logd(message)
        Toast.makeText(
          baseContext,
          message,
          Toast.LENGTH_SHORT
        ).show()

        return@setOnClickListener
      }

      lastRetryTimestamp = currentTime
      startTimer()
      if (mInterstitialAd != null) {
        mInterstitialAd?.show(this)
      } else {
        logd("The interstitial ad wasn't ready yet.")
        loadAd() // Attempt to reload the ad if it wasn't ready
      }
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
          logd("Ad failed to load: ${adError.message}")
          mInterstitialAd = null
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
          logd("Ad was loaded.")
          mInterstitialAd = interstitialAd

          // Set the full screen content callback
          mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
              logd("Ad was dismissed.")
              countdownTimer?.cancel() // Cancel the timer if the ad is dismissed
              if (!timerFinished) {
                // Grant the "easy game" label if ad is dismissed before timer ends
                binding.gameTitle.text = getString(R.string.even_harder)
              }
              mInterstitialAd = null
              loadAd() // Reload the ad after it is dismissed
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
              logd("Ad failed to show.")
            }

            override fun onAdShowedFullScreenContent() {
              logd("Ad showed fullscreen content.")
            }
          }
        }
      }
    )
  }

  private fun startTimer() {
    // Cancel any existing timer
    countdownTimer?.cancel()

    // Reset state
    timerFinished = false
    binding.gameTitle.text = getString(R.string.impossible_game)

    // Start a 5-second countdown
    countdownTimer = object : CountDownTimer(5000, 1000) {
      override fun onTick(millisUntilFinished: Long) {
        binding.timer.text = getString(R.string.timer_seconds, millisUntilFinished / 1000)
      }

      override fun onFinish() {
        timerFinished = true
        binding.gameTitle.text = getString(R.string.easy_game)
        binding.timer.text = getString(R.string.timer_finished)
      }
    }.start()
  }

  override fun onDestroy() {
    super.onDestroy()
    countdownTimer?.cancel() // Clean up timer
  }
}
