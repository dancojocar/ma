package com.example.dan.crashlyticsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val crashlytics = FirebaseCrashlytics.getInstance()
    crashlytics.log("app started")
    setContentView(R.layout.activity_main)
    crashButton.setOnClickListener {
      throw RuntimeException("Test Crash")
    }
  }
}
