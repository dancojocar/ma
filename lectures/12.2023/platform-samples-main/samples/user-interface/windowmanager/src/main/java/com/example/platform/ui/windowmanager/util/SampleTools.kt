/*
 * Copyright 2022 The Android Open Source Project
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
package com.example.platform.ui.windowmanager.util

import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import androidx.window.layout.DisplayFeature
import androidx.window.layout.FoldingFeature
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Offset the [DisplayFeature]'s bounds to match its respective
 * location in the view's coordinate space.
 */
fun adjustFeaturePositionOffset(displayFeature: DisplayFeature, view: View): Rect {
    // Get the location of the view in window to be in the same coordinate space as the feature.
    val viewLocationInWindow = IntArray(2)
    view.getLocationInWindow(viewLocationInWindow)

    // Offset the feature coordinates to view coordinate space start point
    val featureRectInView = Rect(displayFeature.bounds)
    featureRectInView.offset(-viewLocationInWindow[0], -viewLocationInWindow[1])

    return featureRectInView
}

/**
 * Gets the layout params for placing a rectangle indicating a
 * [DisplayFeature] inside a [FrameLayout].
 */
fun getLayoutParamsForFeatureInFrameLayout(displayFeature: DisplayFeature, view: FrameLayout):
    FrameLayout.LayoutParams {
    val featureRectInView = adjustFeaturePositionOffset(displayFeature, view)
    val lp = FrameLayout.LayoutParams(featureRectInView.width(), featureRectInView.height())
    lp.leftMargin = featureRectInView.left
    lp.topMargin = featureRectInView.top
    return lp
}

fun isTableTopMode(foldFeature: FoldingFeature) =
    foldFeature.state == FoldingFeature.State.HALF_OPENED &&
            foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL

fun isBookModeMode(foldFeature: FoldingFeature) =
    foldFeature.state == FoldingFeature.State.HALF_OPENED &&
            foldFeature.orientation == FoldingFeature.Orientation.VERTICAL

/**
 * Returns the position of the fold relative to the view
 */
fun foldPosition(view: View, foldingFeature: FoldingFeature): Int {
    val splitRect = getFeatureBoundsInWindow(foldingFeature, view)
    splitRect?.let {
        return view.height.minus(splitRect.top)
    }
    return 0
}

/**
 * Get the bounds of the display feature translated to the View's coordinate space and current
 * position in the window. This will also include view padding in the calculations.
 */
private fun getFeatureBoundsInWindow(
    displayFeature: DisplayFeature,
    view: View,
    includePadding: Boolean = true
): Rect? {
    // The location of the view in window to be in the same coordinate space as the feature.
    val viewLocationInWindow = IntArray(2)
    view.getLocationInWindow(viewLocationInWindow)

    // Intersect the feature rectangle in window with view rectangle to clip the bounds.
    val viewRect = Rect(
        viewLocationInWindow[0], viewLocationInWindow[1],
        viewLocationInWindow[0] + view.width, viewLocationInWindow[1] + view.height
    )

    // Include padding if needed
    if (includePadding) {
        viewRect.left += view.paddingLeft
        viewRect.top += view.paddingTop
        viewRect.right -= view.paddingRight
        viewRect.bottom -= view.paddingBottom
    }

    val featureRectInView = Rect(displayFeature.bounds)
    val intersects = featureRectInView.intersect(viewRect)

    // Checks to see if the display feature overlaps with our view at all
    if ((featureRectInView.width() == 0 && featureRectInView.height() == 0) ||
        !intersects
    ) {
        return null
    }

    // Offset the feature coordinates to view coordinate space start point
    featureRectInView.offset(-viewLocationInWindow[0], -viewLocationInWindow[1])

    return featureRectInView
}

fun getCurrentTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    val currentDate = sdf.format(Date())
    return currentDate.toString()
}
