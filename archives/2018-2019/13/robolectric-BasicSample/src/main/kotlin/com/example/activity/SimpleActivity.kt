package com.example.activity

import android.app.Activity
import android.os.Bundle
import com.example.R

class SimpleActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.simple)
  }
}
