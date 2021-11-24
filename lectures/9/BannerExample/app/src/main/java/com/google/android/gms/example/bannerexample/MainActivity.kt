package com.google.android.gms.example.bannerexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.example.bannerexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    // Initialize the Mobile Ads SDK with an AdMob App ID.
    MobileAds.initialize(this) {}

    // Set your test devices. Check your logcat output for the hashed device ID to
    // get test ads on a physical device. e.g.
    // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
    // to get test ads on this device."
    MobileAds.setRequestConfiguration(
      RequestConfiguration.Builder()
        .setTestDeviceIds(listOf("33BE2250B43518CCDA7DE426D04EE231"))
        .build()
    )

    // Create an ad request.
    val adRequest = AdRequest.Builder().build()

    // Start loading the ad in the background.
    binding.adView.loadAd(adRequest)
  }

  // Called when leaving the activity
  public override fun onPause() {
    binding.adView.pause()
    super.onPause()
  }

  // Called when returning to the activity
  public override fun onResume() {
    super.onResume()
    binding.adView.resume()
  }

  // Called before the activity is destroyed
  public override fun onDestroy() {
    binding.adView.destroy()
    super.onDestroy()
  }
}
