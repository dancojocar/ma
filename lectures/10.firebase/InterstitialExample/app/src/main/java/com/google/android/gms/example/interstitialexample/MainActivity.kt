package com.google.android.gms.example.interstitialexample

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.example.interstitialexample.databinding.ActivityMainBinding

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  private var mInterstitialAd: InterstitialAd? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    val adRequest = AdRequest.Builder().build()

    InterstitialAd.load(
      this,
      AD_UNIT_ID,
      adRequest,
      object : InterstitialAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          logd(adError.message)
          mInterstitialAd = null
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
          logd("Ad was loaded.")
          mInterstitialAd = interstitialAd
        }
      })

    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        logd("Ad was dismissed.")
      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
        logd("Ad failed to show.")
      }

      override fun onAdShowedFullScreenContent() {
        logd("Ad showed fullscreen content.")
        mInterstitialAd = null
      }
    }

    binding.retryButton.setOnClickListener {

      if (mInterstitialAd != null) {
        mInterstitialAd?.show(this)
      } else {
        logd("The interstitial ad wasn't ready yet.")
      }
    }
  }
}
