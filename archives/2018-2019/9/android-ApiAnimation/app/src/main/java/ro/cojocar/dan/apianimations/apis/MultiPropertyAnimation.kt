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

import android.animation.*
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.LinearLayout
import ro.cojocar.dan.apianimations.R
import java.util.*

/**
 * This application demonstrates the seeking capability of ValueAnimator.
 * The SeekBar in the UI allows you to set the position of the animation.
 * Pressing the Run button will play from the current position of the
 * animation.
 */
class MultiPropertyAnimation : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_multi_property)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener { animView.startAnimation() }

    }

    inner class MyAnimationView(context: Context) : View(context), ValueAnimator.AnimatorUpdateListener {

        val balls = ArrayList<ShapeHolder>()
        internal var animation: AnimatorSet? = null
        internal var bounceAnim: Animator? = null

        init {
            balls.add(addBall(50f, 0f))
            balls.add(addBall(150f, 10f))
            balls.add(addBall(250f, 0f))
            balls.add(addBall(350f, 10f))
        }

        private fun createAnimation() {
            if (bounceAnim == null) {
                var ball: ShapeHolder
                ball = balls[0]
                val yBouncer = ObjectAnimator.ofFloat(
                    ball,
                    "y",
                    ball.y,
                    height - BALL_SIZE
                ).setDuration(DURATION.toLong())
                yBouncer.interpolator = BounceInterpolator()
                yBouncer.addUpdateListener(this)

                ball = balls[1]
                var pvhY = PropertyValuesHolder.ofFloat(
                    "y",
                    ball.y,
                    height - BALL_SIZE
                )
                val pvhAlpha = PropertyValuesHolder.ofFloat(
                    "alpha",
                    1.0f,
                    0f
                )
                val yAlphaBouncer =
                    ObjectAnimator.ofPropertyValuesHolder(ball, pvhY, pvhAlpha).setDuration((DURATION / 2).toLong())
                yAlphaBouncer.interpolator = AccelerateInterpolator()
                yAlphaBouncer.repeatCount = 1
                yAlphaBouncer.repeatMode = ValueAnimator.REVERSE


                ball = balls[2]
                val pvhW = PropertyValuesHolder.ofFloat(
                    "width", ball.width,
                    ball.width * 2
                )
                val pvhH = PropertyValuesHolder.ofFloat(
                    "height", ball.height,
                    ball.height * 2
                )
                val whxyBouncer =
                    ObjectAnimator.ofPropertyValuesHolder(ball, pvhW, pvhH).setDuration((DURATION / 2).toLong())
                whxyBouncer.repeatCount = 1
                whxyBouncer.repeatMode = ValueAnimator.REVERSE

                ball = balls[3]
                pvhY = PropertyValuesHolder.ofFloat(
                    "y",
                    ball.y,
                    height - BALL_SIZE
                )
                val ballX = ball.x
                val kf0 = Keyframe.ofFloat(0f, ballX)
                val kf1 = Keyframe.ofFloat(.5f, ballX + 300f)
                val kf2 = Keyframe.ofFloat(1f, ballX)
                val pvhX = PropertyValuesHolder.ofKeyframe("x", kf0, kf1, kf2)
                val yxBouncer =
                    ObjectAnimator.ofPropertyValuesHolder(ball, pvhY, pvhX).setDuration((DURATION / 2).toLong())
                yxBouncer.repeatCount = 1
                yxBouncer.repeatMode = ValueAnimator.REVERSE


                bounceAnim = AnimatorSet()
                (bounceAnim as AnimatorSet).playTogether(
                    yBouncer,
                    yAlphaBouncer,
                    whxyBouncer,
                    yxBouncer
                )
            }
        }

        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        private fun addBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(BALL_SIZE, BALL_SIZE)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x
            shapeHolder.y = y
            val red = (100 + Math.random() * 155).toInt()
            val green = (100 + Math.random() * 155).toInt()
            val blue = (100 + Math.random() * 155).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = drawable.paint
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
            for (ball in balls) {
                canvas.translate(ball.x, ball.y)
                ball.shape!!.draw(canvas)
                canvas.translate(-ball.x, -ball.y)
            }
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }

    }

    companion object {
        private const val DURATION = 1500
        private const val BALL_SIZE = 100f
    }
}