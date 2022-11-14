/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * An animation that rotates the view on the Y axis between two specified angles.
 * This animation also adds a translation on the Z axis (depth) to improve the effect.
 */
class Rotate3dAnimation
/**
 * Creates a new 3D rotation on the Y axis. The rotation is defined by its
 * start angle and its end angle. Both angles are in degrees. The rotation
 * is performed around a center point on the 2D space, definied by a pair
 * of X and Y coordinates, called centerX and centerY. When the animation
 * starts, a translation on the Z axis (depth) is performed. The length
 * of the translation can be specified, as well as whether the translation
 * should be reversed in time.
 *
 * @param fromDegrees the start angle of the 3D rotation
 * @param toDegrees   the end angle of the 3D rotation
 * @param centerX     the X center of the 3D rotation
 * @param centerY     the Y center of the 3D rotation
 * @param reverse     true if the translation should be reversed, false otherwise
 */
(
    private val fromDegrees: Float, private val toDegrees: Float,
    private val centerX: Float, private val centerY: Float,
    private val depthZ: Float, private val reverse: Boolean
) : Animation() {
  private var mCamera: Camera? = null

  override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
    super.initialize(width, height, parentWidth, parentHeight)
    mCamera = Camera()
  }

  override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
    val fromDegrees = fromDegrees
    val degrees = fromDegrees + (toDegrees - fromDegrees) * interpolatedTime

    val centerX = centerX
    val centerY = centerY
    val camera = mCamera

    val matrix = t.matrix

    camera!!.save()
    if (reverse) {
      camera.translate(0.0f, 0.0f, depthZ * interpolatedTime)
    } else {
      camera.translate(0.0f, 0.0f, depthZ * (1.0f - interpolatedTime))
    }
    camera.rotateY(degrees)
    camera.getMatrix(matrix)
    camera.restore()

    matrix.preTranslate(-centerX, -centerY)
    matrix.postTranslate(centerX, centerY)
  }
}
