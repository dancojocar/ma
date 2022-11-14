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
package com.example.canvasexample

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

/**
 * Custom view that follows touch events to draw on a canvas.
 */
class MyCanvasView(context: Context?) : View(context) {
  private val mPaint: Paint = Paint()
  private val mPath: Path = Path()
  private val mDrawColor: Int = ResourcesCompat.getColor(resources,
      R.color.opaque_yellow, null)
  private val mBackgroundColor: Int = ResourcesCompat.getColor(resources,
      R.color.opaque_orange, null)
  private var mExtraCanvas: Canvas? = null
  private var mExtraBitmap: Bitmap? = null
  private var mFrame: Rect? = null

  /**
   * Note: Called whenever the view changes size.
   * Since the view starts out with no size, this is also called after
   * the view has been inflated and has a valid size.
   */
  override fun onSizeChanged(width: Int, height: Int,
                             oldWidth: Int, oldHeight: Int) {
    super.onSizeChanged(width, height, oldWidth, oldHeight)
    // Create bitmap, create canvas with bitmap, fill canvas with color.
    mExtraBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    mExtraCanvas = Canvas(mExtraBitmap!!)
    mExtraCanvas!!.drawColor(mBackgroundColor)
    // Calculate the rect a frame around the picture.
    val inset = 40
    mFrame = Rect(inset, inset, width - inset, height - inset)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    // Draw the bitmap that has the saved path.
    canvas.drawBitmap(mExtraBitmap!!, 0f, 0f, null)
    // Draw a frame around the picture.
    canvas.drawRect(mFrame!!, mPaint)
  }

  // Variables for the latest x,y values,
  // which are the starting point for the next path.
  private var mX = 0f
  private var mY = 0f
  // The following methods factor out what happens for different touch events,
  // as determined by the onTouchEvent() switch statement.
  // This keeps the switch statement
  // concise and easier to change what happens for each event.
  private fun touchStart(x: Float, y: Float) {
    mPath.moveTo(x, y)
    mX = x
    mY = y
  }

  private fun touchMove(x: Float, y: Float) {
    val dx = abs(x - mX)
    val dy = abs(y - mY)
    if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
      // QuadTo() adds a quadratic bezier from the last point,
      // approaching control point (x1,y1), and ending at (x2,y2).
      mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
      mX = x
      mY = y
      // Draw the path in the extra bitmap to save it.
      mExtraCanvas!!.drawPath(mPath, mPaint)
    }
  }

  private fun touchUp() { // Reset the path so it doesn't get drawn again.
    mPath.reset()
  }

  override fun performClick(): Boolean {
    return super.performClick()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val x = event.x
    val y = event.y
    when (event.action) {
      MotionEvent.ACTION_DOWN -> touchStart(x, y)
      MotionEvent.ACTION_MOVE -> {
        touchMove(x, y)
        invalidate()
      }
      MotionEvent.ACTION_UP -> touchUp()
      else -> {
        performClick()
      }
    }
    return true
  }

  companion object {
    // Don't draw every single pixel.
    // If the finger has has moved less than this distance, don't draw.
    private const val TOUCH_TOLERANCE = 4f
  }

  init {
    // Holds the path we are currently drawing.
    // Set up the paint with which to draw.
    mPaint.color = mDrawColor
    // Smoothes out edges of what is drawn without affecting shape.
    mPaint.isAntiAlias = true
    // Dithering affects how colors with higher-precision
    // than the device are down-sampled.
    mPaint.isDither = true
    mPaint.style = Paint.Style.STROKE // default: FILL
    mPaint.strokeJoin = Paint.Join.ROUND // default: MITER
    mPaint.strokeCap = Paint.Cap.ROUND // default: BUTT
    mPaint.strokeWidth = 12f // default: Hairline-width (really thin)
  }
}