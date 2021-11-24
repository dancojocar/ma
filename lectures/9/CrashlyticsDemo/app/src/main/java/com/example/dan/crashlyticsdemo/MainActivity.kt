package com.example.dan.crashlyticsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.dan.crashlyticsdemo.databinding.ActivityMainBinding
import com.google.firebase.crashlytics.FirebaseCrashlytics

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val crashlytics = FirebaseCrashlytics.getInstance()
    crashlytics.log("app started")
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    binding.crashButton.setOnClickListener {
      throw RuntimeException("Test Crash")
    }
  }
}
