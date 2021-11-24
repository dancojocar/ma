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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import ro.cojocar.dan.apianimations.R
import ro.cojocar.dan.apianimations.databinding.LayoutAnimationsHideshowBinding

/**
 * This application demonstrates how to use LayoutTransition to
 * automate transition animations as items are hidden or shown
 * in a container.
 */
class LayoutAnimationsHideShow : Activity() {
  private lateinit var binding: LayoutAnimationsHideshowBinding

  internal var container: ViewGroup? = null
  private var mTransitioner: LayoutTransition? = null

  /**
   * Called when the activity is first created.
   */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = LayoutAnimationsHideshowBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    container = LinearLayout(this)
    container!!.layoutParams = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    // Add a slew of buttons to the container.
    // We won't add any more buttons at runtime, but
    // will just show/hide the buttons we've already created
    val numButtons = 4
    for (i in 0 until numButtons) {
      val newButton = Button(this)
      newButton.text = i.toString()
      container!!.addView(newButton)
      newButton.setOnClickListener { v ->
        v.visibility = if (binding.hideGoneCB.isChecked)
          View.GONE
        else
          View.INVISIBLE
      }
    }

    resetTransition()

    val parent = findViewById<ViewGroup>(R.id.parent)
    parent.addView(container)

    binding.addNewButton.setOnClickListener {
      for (i in 0 until container!!.childCount) {
        val view = container!!.getChildAt(i)
        view.visibility = View.VISIBLE
      }
    }

    binding.customAnimCB.setOnCheckedChangeListener { _, isChecked ->
      val duration: Long = if (isChecked) {
        mTransitioner!!.setStagger(
            LayoutTransition.CHANGE_APPEARING,
            300
        )
        mTransitioner!!.setStagger(
            LayoutTransition.CHANGE_DISAPPEARING,
            300
        )
        setupCustomAnimations()
        500
      } else {
        resetTransition()
        300
      }
      mTransitioner!!.setDuration(duration)
    }
  }

  private fun resetTransition() {
    mTransitioner = LayoutTransition()
    container!!.layoutTransition = mTransitioner
  }

  private fun setupCustomAnimations() {
    // Adding
    val animIn = ObjectAnimator.ofFloat(
        null,
        "rotationY", 90f, 0f
    ).setDuration(mTransitioner!!.getDuration(LayoutTransition.APPEARING))
    mTransitioner!!.setAnimator(LayoutTransition.APPEARING, animIn)
    animIn.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(anim: Animator) {
        val view = (anim as ObjectAnimator).target as View
        view.rotationY = 0f
      }
    })

    // Removing
    val animOut = ObjectAnimator.ofFloat(
        null,
        "rotationX", 0f, 90f
    ).setDuration(mTransitioner!!.getDuration(LayoutTransition.DISAPPEARING))
    mTransitioner!!.setAnimator(LayoutTransition.DISAPPEARING, animOut)
    animOut.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(anim: Animator) {
        val view = (anim as ObjectAnimator).target as View
        view.rotationX = 0f
      }
    })
  }
}