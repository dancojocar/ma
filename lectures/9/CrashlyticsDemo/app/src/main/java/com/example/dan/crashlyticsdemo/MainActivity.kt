package com.example.dan.crashlyticsdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Crashlytics.log("app started")
    setContentView(R.layout.activity_main)
    crashButton.setOnClickListener {
      Crashlytics.getInstance().crash()
    }
  }
}
