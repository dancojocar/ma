package com.example.dan.memoryleakdemo

import android.app.Activity
import android.os.Bundle
import android.os.SystemClock
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : Activity() {

  private var httpRequestHelper: HttpRequestHelper? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    fab.setOnClickListener {
      startAsyncWork()
    }

    httpRequestHelper = lastNonConfigurationInstance as HttpRequestHelper?
    if (httpRequestHelper == null) {
      httpRequestHelper = HttpRequestHelper(fab)
    }
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
      SystemClock.sleep(20000)
    }
    Thread(work).start()
  }
}