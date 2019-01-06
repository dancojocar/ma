/*
* Copyright 2013 The Android Open Source Project
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
/*
 * Copyright 2013 The Android Open Source Project
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

package com.example.android.common.logger

import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView

/**
 * Simple fragment which contains a LogView and uses is
 * to output log data it receives to the screen.
 */
class LogFragment : Fragment() {

  lateinit var logView: LogView
    private set
  private lateinit var mScrollView: ScrollView

  private fun inflateViews(): View {
    mScrollView = ScrollView(activity)
    val scrollParams = ViewGroup.LayoutParams(
      ViewGroup.LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT
    )
    mScrollView.layoutParams = scrollParams

    logView = LogView(context!!)
    val logParams = ViewGroup.LayoutParams(scrollParams)
    logParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
    logView.layoutParams = logParams
    logView.isClickable = true
    logView.isFocusable = true
    logView.typeface = Typeface.MONOSPACE

    // Want to set padding as 16 dips, setPadding takes pixels.  Hooray math!
    val paddingDips = 16
    val scale = resources.displayMetrics.density.toDouble()
    val paddingPixels = (paddingDips * scale + .5).toInt()
    logView.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels)
    logView.compoundDrawablePadding = paddingPixels

    logView.gravity = Gravity.BOTTOM
    logView.setTextAppearance(
      android.R.style.TextAppearance_DeviceDefault_Medium
    )

    mScrollView.addView(logView)
    return mScrollView
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {

    val result = inflateViews()

    logView.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: Editable) {
        mScrollView.fullScroll(ScrollView.FOCUS_DOWN)
      }
    })
    return result
  }
}