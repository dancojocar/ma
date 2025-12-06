/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.samples.gridtopager

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.google.samples.gridtopager.databinding.ActivityMainBinding
import com.google.samples.gridtopager.fragment.GridFragment

/**
 * Grid to pager app's main activity.
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainer) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        insets
    }

    if (savedInstanceState != null) {
      currentPosition = savedInstanceState.getInt(KEY_CURRENT_POSITION, 0)
      // Return here to prevent adding additional GridFragments when changing orientation.
      return
    }
    val fragmentManager = supportFragmentManager
    fragmentManager
        .beginTransaction()
        .add(binding.fragmentContainer.id, GridFragment(), GridFragment::class.java.getSimpleName())
        .commit()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt(KEY_CURRENT_POSITION, currentPosition)
  }

  companion object {

    /**
     * Holds the current image position to be shared between the grid and the pager fragments. This
     * position updated when a grid item is clicked, or when paging the pager.
     *
     * In this demo app, the position always points to an image index at the [ ] class.
     */
    var currentPosition: Int = 0
    private const val KEY_CURRENT_POSITION = "com.google.samples.gridtopager.key.currentPosition"
  }
}
