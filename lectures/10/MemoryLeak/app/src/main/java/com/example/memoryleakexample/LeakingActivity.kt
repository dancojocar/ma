package com.example.memoryleakexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class LeakingActivity : AppCompatActivity() {

    private val listener = Listener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaking)
    }

    override fun onStart() {
        super.onStart()
        GlobalSingleton.register(listener)
    }

    override fun onStop() {
        super.onStop()
        // GlobalSingleton.unregister(listener)
    }

    // inner class has implicit reference to enclosing Activity
    private inner class Listener : GlobalSingletonListener {
        override fun onEvent() {}
    }
}