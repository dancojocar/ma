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

import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.Shape

/**
 * A data structure that holds a Shape and various properties that can be used to define
 * how the shape is drawn.
 */
class ShapeHolder(var shape: ShapeDrawable?) {
    var x = 0f
    var y = 0f
    var color: Int = 0
        set(value) {
            shape!!.paint.color = value
            field = value
        }
    var gradient: RadialGradient? = null
    private var alpha = 1f
    var paint: Paint? = null

    var width: Float
        get() = shape!!.shape.width
        set(width) {
            val s = shape!!.shape
            s.resize(width, s.height)
        }

    var height: Float
        get() = shape!!.shape.height
        set(height) {
            val s = shape!!.shape
            s.resize(s.width, height)
        }

    fun setAlpha(alpha: Float) {
        this.alpha = alpha
        shape!!.alpha = (alpha * 255f + .5f).toInt()
    }
}
