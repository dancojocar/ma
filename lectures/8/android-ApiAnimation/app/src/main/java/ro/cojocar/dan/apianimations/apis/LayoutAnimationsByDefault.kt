/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.cojocar.dan.apianimations.apis

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import ro.cojocar.dan.apianimations.R
import ro.cojocar.dan.apianimations.databinding.LayoutAnimationsByDefaultBinding
import kotlin.math.min

/**
 * This application demonstrates how to use the animateLayoutChanges tag
 * in XML to automate transition animations as items are removed from or
 * added to a container.
 */
class LayoutAnimationsByDefault : Activity() {
  private lateinit var binding: LayoutAnimationsByDefaultBinding

  private var numButtons = 1

  public override fun onCreate(savedInstanceState: Bundle?) {

    super.onCreate(savedInstanceState)
    binding = LayoutAnimationsByDefaultBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    val gridContainer = binding.gridContainer

    val addButton = findViewById<Button>(R.id.addNewButton)
    addButton.setOnClickListener {
      val newButton = Button(this@LayoutAnimationsByDefault)
      newButton.text = numButtons++.toString()
      newButton.setOnClickListener { v -> gridContainer.removeView(v) }
      gridContainer.addView(newButton, min(1, gridContainer.childCount))
    }
  }
}