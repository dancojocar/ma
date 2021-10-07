package com.example.myapplication

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logd("onCreate was reached!")
    }

    override fun onStart() {
        super.onStart()
        logd("onStart was reached!")
    }

    override fun onResume() {
        super.onResume()
        logd("onResume was reached!")
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
