/*
 * Copyright (C) 2009 The Android Open Source Project
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

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * A layout that arranges its children in a grid.  The size of the
 * cells is set by the [.setCellSize] method and the
 * android:cell_width and android:cell_height attributes in XML.
 * The number of rows and columns is determined at runtime.  Each
 * cell contains exactly one view, and they flow in the natural
 * child order (the order in which they were added, or the index
 * in [.addViewAt].  Views can not span multiple cells.
 *
 *
 *
 * This class was copied from the FixedGridLayout Api demo; see that demo for
 * more information on using the layout.
 */
class FixedGridLayout(context: Context) : ViewGroup(context) {
    internal var mCellWidth: Int = 0
    internal var mCellHeight: Int = 0

    fun setCellWidth(px: Int) {
        mCellWidth = px
        requestLayout()
    }

    fun setCellHeight(px: Int) {
        mCellHeight = px
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cellWidthSpec = View.MeasureSpec.makeMeasureSpec(
            mCellWidth,
            View.MeasureSpec.AT_MOST
        )
        val cellHeightSpec = View.MeasureSpec.makeMeasureSpec(
            mCellHeight,
            View.MeasureSpec.AT_MOST
        )

        val count = childCount
        for (index in 0 until count) {
            val child = getChildAt(index)
            child.measure(cellWidthSpec, cellHeightSpec)
        }
        // Use the size our parents gave us, but default to a minimum size to avoid
        // clipping transitioning children
        val minCount = if (count > 3) count else 3
        setMeasuredDimension(
            View.resolveSize(mCellWidth * minCount, widthMeasureSpec),
            View.resolveSize(mCellHeight * minCount, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val cellWidth = mCellWidth
        val cellHeight = mCellHeight
        var columns = (r - l) / cellWidth
        if (columns < 0) {
            columns = 1
        }
        var x = 0
        var y = 0
        var i = 0
        val count = childCount
        for (index in 0 until count) {
            val child = getChildAt(index)

            val w = child.measuredWidth
            val h = child.measuredHeight

            val left = x + (cellWidth - w) / 2
            val top = y + (cellHeight - h) / 2

            child.layout(left, top, left + w, top + h)
            if (i >= columns - 1) {
                // advance to next row
                i = 0
                x = 0
                y += cellHeight
            } else {
                i++
                x += cellWidth
            }
        }
    }
}

