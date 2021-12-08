/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.memoryoverload

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.android.memoryoverload.databinding.ActivityMainBinding
import java.util.*
import com.google.android.flexbox.FlexDirection

import android.R
import com.google.android.flexbox.FlexWrap

import com.google.android.flexbox.FlexboxLayout


/**
 * Demo app to fill up available memory.
 * After the app opens, tap to allocate rows of new objects.
 * When the memory available to the app is used up, the app will crash.
 * Used for demonstrating Android Profiler tools.
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    setSupportActionBar(binding.toolbar)
  }

  /**
   * Adds a new row of text views when the floating action button is pressed.
   */
  fun addRowOfTextViews(view: View?) {
    val root = binding.contentMain.rootLinearLayout
    val flexboxLayout = FlexboxLayout(view?.context)
    flexboxLayout.flexDirection = FlexDirection.ROW
    flexboxLayout.flexWrap = FlexWrap.WRAP
    val textViews = mutableListOf<TextView>()
    for (i in 0 until NO_OF_TEXTVIEWS_ADDED) {
      val tv = TextView(this)
      textViews.add(tv)
      tv.text = i.toString()
      tv.setBackgroundColor(randomColor)
      flexboxLayout.addView(tv)
    }
    root.addView(flexboxLayout)
  }

  /**
   * Creates a random color for background color of the text view.
   */
  private val randomColor: Int
    get() {
      val r = Random()
      val red = r.nextInt(255)
      val green = r.nextInt(255)
      val blue = r.nextInt(255)
      return Color.rgb(red, green, blue)
    }

  companion object {
    const val NO_OF_TEXTVIEWS_ADDED = 10000
  }
}