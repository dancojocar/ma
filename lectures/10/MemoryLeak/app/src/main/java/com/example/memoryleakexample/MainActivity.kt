package com.example.memoryleakexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private val btnStartNewActivity by lazy { findViewById<Button>(R.id.btnStartNewActivity)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartNewActivity.setOnClickListener {
            startActivity(Intent(this,LeakingActivity::class.java))
        }
    }
}
