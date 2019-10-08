package com.example.dan.memoryleakdemo

import android.app.Application
import android.os.StrictMode
import com.squareup.leakcanary.LeakCanary

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupLeakCanary()
    }

    protected fun setupLeakCanary() {
        enabledStrictMode()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        LeakCanary.install(this)
    }

    private fun enabledStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder() //
                .detectAll() //
                .penaltyLog() //
                .penaltyDeath() //
                .build()
        )
    }
}