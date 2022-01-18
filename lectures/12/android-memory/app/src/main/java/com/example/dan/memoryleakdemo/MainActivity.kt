package com.example.dan.memoryleakdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import com.example.dan.memoryleakdemo.databinding.ActivityMainBinding


class MainActivity : Activity() {
  private lateinit var binding: ActivityMainBinding

  private var httpRequestHelper: HttpRequestHelper? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.fab.setOnClickListener {
      startAsyncWork()
    }

    httpRequestHelper = lastNonConfigurationInstance as HttpRequestHelper?
    if (httpRequestHelper == null) {
      httpRequestHelper = HttpRequestHelper(binding.fab)
    }
    val textView = findViewById<View>(R.id.tv)

    val app = application as ExampleApplication
    // This creates a leak, What a Terrible Failure!
    app.leakedViews.add(textView)
  }

  override fun onRetainNonConfigurationInstance(): Any? {
    return httpRequestHelper
  }

  private fun startAsyncWork() {
    // This runnable is an anonymous class and therefore has a hidden reference to the outer
    // class MainActivity. If the activity gets destroyed before the thread finishes (e.g. rotation),
    // the activity instance will leak.
    val work = Runnable {
      // Do some slow work in background
      SystemClock.sleep(2000)
      val intent= Intent(this, MainActivity::class.java)
      startActivity(intent)
    }
    Thread(work).start()
  }
}