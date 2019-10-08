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

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import ro.cojocar.dan.apianimations.R

class ReversingAnimation : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_reversing)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener { animView.startAnimation() }

        val reverser = findViewById<Button>(R.id.reverseButton)
        reverser.setOnClickListener { animView.reverseAnimation() }

    }

    inner class MyAnimationView(context: Context) : View(context), ValueAnimator.AnimatorUpdateListener {

        internal var bounceAnim: ValueAnimator? = null
        internal var ball: ShapeHolder? = null

        init {
            ball = createBall()
        }

        private fun createAnimation() {
            if (bounceAnim == null) {
                bounceAnim = ObjectAnimator.ofFloat(
                    ball,
                    "y", ball!!.y, height - 50f
                ).setDuration(1500)
                bounceAnim!!.interpolator = AccelerateInterpolator(2f)
                bounceAnim!!.addUpdateListener(this)
            }
        }

        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        fun reverseAnimation() {
            createAnimation()
            bounceAnim!!.reverse()
        }

        private fun createBall(): ShapeHolder {
            val circle = OvalShape()
            circle.resize(50f, 50f)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = 0f
            shapeHolder.y = 0f
            val red = (Math.random() * 255).toInt()
            val green = (Math.random() * 255).toInt()
            val blue = (Math.random() * 255).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = drawable.paint //new Paint(Paint.ANTI_ALIAS_FLAG);
            val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
            val gradient = RadialGradient(
                37.5f, 12.5f,
                50f, color, darkColor, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            shapeHolder.paint = paint
            return shapeHolder
        }

        override fun onDraw(canvas: Canvas) {
            canvas.save()
            canvas.translate(ball!!.x, ball!!.y)
            ball!!.shape!!.draw(canvas)
            canvas.restore()
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }

    }
}