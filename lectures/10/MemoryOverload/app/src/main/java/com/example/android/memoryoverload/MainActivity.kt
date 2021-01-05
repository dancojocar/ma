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
import java.util.*

/**
 * Demo app to fill up available memory.
 * After the app opens, tap to allocate rows of new objects.
 * When the memory available to the app is used up, the app will crash.
 * Used for demonstrating Android Profiler tools.
 */
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
  }

  /**
   * Adds a new row of text views when the floating action button is pressed.
   */
  fun addRowOfTextViews(view: View?) {
    val root = findViewById<LinearLayout>(R.id.rootLinearLayout)
    val linearLayout = LinearLayout(this)
    linearLayout.orientation = LinearLayout.HORIZONTAL
    val linearLayoutParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT)
    linearLayout.layoutParams = linearLayoutParams
    val textViewParams = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT)
    val textViews = arrayOfNulls<TextView>(NO_OF_TEXTVIEWS_ADDED)
    for (i in 0 until NO_OF_TEXTVIEWS_ADDED) {
      textViews[i] = TextView(this)
      textViews[i]!!.layoutParams = textViewParams
      textViews[i]!!.text = i.toString()
      textViews[i]!!.setBackgroundColor(randomColor)
      linearLayout.addView(textViews[i])
    }
    root.addView(linearLayout)
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