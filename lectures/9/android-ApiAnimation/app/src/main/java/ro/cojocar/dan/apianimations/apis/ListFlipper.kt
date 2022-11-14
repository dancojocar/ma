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
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.ListView
import ro.cojocar.dan.apianimations.databinding.RotatingListBinding

class ListFlipper : Activity() {
  private lateinit var binding: RotatingListBinding

  private lateinit var mEnglishList: ListView
  private lateinit var mRomanianList: ListView
  private val accelerator = AccelerateInterpolator()
  private val decelerator = DecelerateInterpolator()

  /**
   * Called when the activity is first created.
   */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = RotatingListBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    mEnglishList = binding.listEn
    mRomanianList = binding.listRo

    // Prepare the ListView
    val adapterEn = ArrayAdapter(
      this,
      android.R.layout.simple_list_item_1, LIST_STRINGS_EN
    )
    // Prepare the ListView
    val adapterRo = ArrayAdapter(
      this,
      android.R.layout.simple_list_item_1, LIST_STRINGS_RO
    )

    mEnglishList.adapter = adapterEn
    mRomanianList.adapter = adapterRo
    mRomanianList.rotationY = -90f

    val starter = binding.button
    starter.setOnClickListener { flip() }
  }

  private fun flip() {
    val visibleList: ListView
    val invisibleList: ListView
    if (mEnglishList.visibility == View.GONE) {
      visibleList = mRomanianList
      invisibleList = mEnglishList
    } else {
      invisibleList = mRomanianList
      visibleList = mEnglishList
    }
    val visibleToInvisible = ObjectAnimator.ofFloat(
      visibleList,
      "rotationY", 0f, 90f
    )
    visibleToInvisible.duration = DURATION.toLong()
    visibleToInvisible.interpolator = accelerator

    val invisibleToVisible = ObjectAnimator.ofFloat(
      invisibleList,
      "rotationY", -90f, 0f
    )
    invisibleToVisible.duration = DURATION.toLong()
    invisibleToVisible.interpolator = decelerator

    visibleToInvisible.addListener(object : AnimatorListenerAdapter() {
      override fun onAnimationEnd(anim: Animator) {
        visibleList.visibility = View.GONE
        invisibleToVisible.start()
        invisibleList.visibility = View.VISIBLE
      }
    })
    visibleToInvisible.start()
  }

  companion object {

    private const val DURATION = 1500

    private val LIST_STRINGS_EN: Array<String> =
      arrayOf("One", "Two", "Three", "Four", "Five", "Six")
    private val LIST_STRINGS_RO: Array<String> =
      arrayOf("Unu", "Doi", "Trei", "Patru", "Cinci", "Șase")
  }


}