package com.example.myapplication

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        logd("OnCreate was reached!")
    }

    override fun onPause() {
        super.onPause()
        logd("onPause was reached!")
    }

    override fun onRestart() {
        super.onRestart()
        logd("onRestart was reached!")
    }
}
