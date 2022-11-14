/*
 *   Copyright (c) 2019 Google Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed under
 *   the License
 *
 *   is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.android.example.filelocker

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.updateLayoutParams
import androidx.databinding.BindingAdapter
import com.google.android.material.shape.MaterialShapeDrawable


@BindingAdapter("statusBarAdaptive")
fun View.bindStatusBarAdaptive(statusBarAdaptive: Boolean) {
  val bgColor = backgroundColor
  val luminance = ColorUtils.calculateLuminance(bgColor)
  if (luminance < 0.5) {
    clearStatusBarLight()
  } else {
    setStatusBarLight()
  }
}

private val View.backgroundColor: Int
  @ColorInt get() {
    return when (val bg = background) {
      is ColorDrawable -> bg.color
      is MaterialShapeDrawable -> bg.fillColor?.defaultColor ?: Color.TRANSPARENT
      else -> Color.TRANSPARENT
    }
  }

private fun View.setStatusBarLight() {
  windowInsetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
}

private fun View.clearStatusBarLight() {
  windowInsetsController?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS.inv(), APPEARANCE_LIGHT_STATUS_BARS)
}

@BindingAdapter("layoutFullscreen")
fun View.bindLayoutFullscreen(previousFullscreen: Boolean, fullscreen: Boolean) {
  if (previousFullscreen != fullscreen && fullscreen) {
    val controller = windowInsetsController
    if (controller != null) {
      controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
      controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  }
}

@BindingAdapter(
    "paddingLeftSystemWindowInsets",
    "paddingTopSystemWindowInsets",
    "paddingRightSystemWindowInsets",
    "paddingBottomSystemWindowInsets",
    requireAll = false
)
fun View.applySystemWindowInsetsPadding(
    previousApplyLeft: Boolean,
    previousApplyTop: Boolean,
    previousApplyRight: Boolean,
    previousApplyBottom: Boolean,
    applyLeft: Boolean,
    applyTop: Boolean,
    applyRight: Boolean,
    applyBottom: Boolean
) {
  if (previousApplyLeft == applyLeft &&
      previousApplyTop == applyTop &&
      previousApplyRight == applyRight &&
      previousApplyBottom == applyBottom
  ) {
    return
  }

  doOnApplyWindowInsets { view, insets, padding, _, _ ->
    val left = if (applyLeft) insets.systemWindowInsetLeft else 0
    val top = if (applyTop) insets.systemWindowInsetTop else 0
    val right = if (applyRight) insets.systemWindowInsetRight else 0
    val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0

    view.setPadding(
        padding.left + left,
        padding.top + top,
        padding.right + right,
        padding.bottom + bottom
    )
  }
}

@BindingAdapter(
    "marginLeftSystemWindowInsets",
    "marginTopSystemWindowInsets",
    "marginRightSystemWindowInsets",
    "marginBottomSystemWindowInsets",
    requireAll = false
)
fun View.applySystemWindowInsetsMargin(
    previousApplyLeft: Boolean,
    previousApplyTop: Boolean,
    previousApplyRight: Boolean,
    previousApplyBottom: Boolean,
    applyLeft: Boolean,
    applyTop: Boolean,
    applyRight: Boolean,
    applyBottom: Boolean
) {
  if (previousApplyLeft == applyLeft &&
      previousApplyTop == applyTop &&
      previousApplyRight == applyRight &&
      previousApplyBottom == applyBottom
  ) {
    return
  }

  doOnApplyWindowInsets { view, insets, _, margin, _ ->
    val left = if (applyLeft) insets.systemWindowInsetLeft else 0
    val top = if (applyTop) insets.systemWindowInsetTop else 0
    val right = if (applyRight) insets.systemWindowInsetRight else 0
    val bottom = if (applyBottom) insets.systemWindowInsetBottom else 0

    view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
      leftMargin = margin.left + left
      topMargin = margin.top + top
      rightMargin = margin.right + right
      bottomMargin = margin.bottom + bottom
    }
  }
}

fun View.doOnApplyWindowInsets(
    block: (View, WindowInsets, InitialPadding, InitialMargin, Int) -> Unit
) {
  // Create a snapshot of the view's padding & margin states
  val initialPadding = recordInitialPaddingForView(this)
  val initialMargin = recordInitialMarginForView(this)
  val initialHeight = recordInitialHeightForView(this)
  // Set an actual OnApplyWindowInsetsListener which proxies to the given
  // lambda, also passing in the original padding & margin states
  setOnApplyWindowInsetsListener { v, insets ->
    block(v, insets, initialPadding, initialMargin, initialHeight)
    // Always return the insets, so that children can also use them
    insets
  }
  // request some insets
  requestApplyInsetsWhenAttached()
}

class InitialPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)

class InitialMargin(val left: Int, val top: Int, val right: Int, val bottom: Int)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private fun recordInitialMarginForView(view: View): InitialMargin {
  val lp = view.layoutParams as? ViewGroup.MarginLayoutParams
      ?: throw IllegalArgumentException("Invalid view layout params")
  return InitialMargin(lp.leftMargin, lp.topMargin, lp.rightMargin, lp.bottomMargin)
}

private fun recordInitialHeightForView(view: View): Int {
  return view.layoutParams.height
}

fun View.requestApplyInsetsWhenAttached() {
  if (isAttachedToWindow) {
    // We're already attached, just request as normal
    requestApplyInsets()
  } else {
    // We're not attached to the hierarchy, add a listener to
    // request when we are
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
      override fun onViewAttachedToWindow(v: View) {
        v.removeOnAttachStateChangeListener(this)
        v.requestApplyInsets()
      }

      override fun onViewDetachedFromWindow(v: View) = Unit
    })
  }
}