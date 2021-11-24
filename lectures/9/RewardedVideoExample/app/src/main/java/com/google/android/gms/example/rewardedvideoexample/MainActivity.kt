package com.google.android.gms.example.rewardedvideoexample

import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.example.rewardedvideoexample.databinding.ActivityMainBinding

const val AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
const val COUNTER_TIME = 10L
const val GAME_OVER_REWARD = 1

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  private var mCoinCount: Int = 0
  private var mCountDownTimer: CountDownTimer? = null
  private var mGameOver = false
  private var mGamePaused = false
  private var mIsLoading = false
  private var mRewardedAd: RewardedAd? = null
  private var mTimeRemaining: Long = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    MobileAds.initialize(this) {}

    loadRewardedAd()

    // Create the "retry" button, which tries to show a rewarded video ad between game plays.
    binding.retryButton.visibility = View.INVISIBLE
    binding.retryButton.setOnClickListener { startGame() }

    // Create the "show" button, which shows a rewarded video if one is loaded.
    binding.showVideoButton.visibility = View.INVISIBLE
    binding.showVideoButton.setOnClickListener { showRewardedVideo() }

    // Display current coin count to user.
    binding.coinCountText.text = "Coins: $mCoinCount"

    startGame()
  }

  public override fun onPause() {
    super.onPause()
    pauseGame()
  }

  public override fun onResume() {
    super.onResume()
    if (!mGameOver && mGamePaused) {
      resumeGame()
    }
  }

  private fun pauseGame() {
    mCountDownTimer?.cancel()
    mGamePaused = true
  }

  private fun resumeGame() {
    createTimer(mTimeRemaining)
    mGamePaused = false
  }

  private fun loadRewardedAd() {
    val adRequest = AdRequest.Builder().build()

    RewardedAd.load(this, AD_UNIT_ID,
      adRequest, object : RewardedAdLoadCallback() {
        override fun onAdFailedToLoad(adError: LoadAdError) {
          logd(adError.message)
          mRewardedAd = null
        }

        override fun onAdLoaded(rewardedAd: RewardedAd) {
          logd("Ad was loaded.")
          mRewardedAd = rewardedAd
        }
      })
  }

  private fun addCoins(coins: Int) {
    mCoinCount += coins
    binding.coinCountText.text = "Coins: $mCoinCount"
  }

  private fun startGame() {
    // Hide the retry button, load the ad, and start the timer.
    binding.retryButton.visibility = View.INVISIBLE
    binding.showVideoButton.visibility = View.INVISIBLE
    if (mRewardedAd == null && !mIsLoading) {
      loadRewardedAd()
    }
    createTimer(COUNTER_TIME)
    mGamePaused = false
    mGameOver = false
  }

  // Create the game timer, which counts down to the end of the level
  // and shows the "retry" button.
  private fun createTimer(time: Long) {
    mCountDownTimer?.cancel()

    mCountDownTimer = object : CountDownTimer(time * 1000, 50) {
      override fun onTick(millisUnitFinished: Long) {
        mTimeRemaining = millisUnitFinished / 1000 + 1
        binding.timer.text = "seconds remaining: $mTimeRemaining"
      }

      override fun onFinish() {
        if (mRewardedAd != null) {
          binding.showVideoButton.visibility = View.VISIBLE
        }
        binding.timer.text = "The game has ended!"
        addCoins(GAME_OVER_REWARD)
        binding.retryButton.visibility = View.VISIBLE
        mGameOver = true
      }
    }

    mCountDownTimer?.start()
  }

  private fun showRewardedVideo() {
    binding.showVideoButton.visibility = View.INVISIBLE
    if (mRewardedAd != null) {
      mRewardedAd?.show(this) {
        val rewardAmount = it.amount
//          var rewardType = rewardItem.getType()
        addCoins(rewardAmount)
        logd("User earned the reward.")
        mRewardedAd = null
      }
    } else {
      logd("The rewarded ad wasn't ready yet.")
    }
  }
}
