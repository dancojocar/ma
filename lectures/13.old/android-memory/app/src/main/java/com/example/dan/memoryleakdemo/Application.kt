package com.example.dan.memoryleakdemo

import android.app.Application
import android.view.View

class ExampleApplication : Application() {
  val leakedViews = mutableListOf<View>()

  private fun setupLeakCanary() {
//    enabledStrictMode()
//    if (LeakCanary.isInAnalyzerProcess(this)) {
    // This process is dedicated to LeakCanary for heap analysis.
    // You should not init your app in this process.
//      return
//    }
//    LeakCanary.install(this)
  }
}