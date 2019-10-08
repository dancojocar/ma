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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import ro.cojocar.dan.apianimations.R

/**
 * This application demonstrates the seeking capability of ValueAnimator.
 * The SeekBar in the UI allows you to set the position of the animation.
 * Pressing the Run button will play from the current position of the
 * animation.
 */
class AnimationSeeking : Activity() {
    private var mSeekBar: SeekBar? = null

    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_seeking)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener { animView.startAnimation() }

        mSeekBar = findViewById(R.id.seekBar)
        mSeekBar!!.max = DURATION
        mSeekBar!!.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {}

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onProgressChanged(
                    seekBar: SeekBar, progress: Int,
                    fromUser: Boolean
                ) {
                    // prevent seeking on app creation
                    if (animView.height != 0) {
                        animView.seek(progress.toLong())
                    }
                }
            })
    }

    inner class MyAnimationView(context: Context) : View(context), ValueAnimator.AnimatorUpdateListener,
        Animator.AnimatorListener {

        internal var animation: AnimatorSet? = null
        internal var bounceAnim: ValueAnimator? = null
        internal var ball: ShapeHolder? = null

        init {
            ball = addBall(200f, 0f)
        }

        private fun createAnimation() {
            if (bounceAnim == null) {
                bounceAnim = ObjectAnimator.ofFloat(
                    ball,
                    "y",
                    ball!!.y, height - BALL_SIZE
                ).setDuration(DURATION.toLong())
                bounceAnim!!.interpolator = BounceInterpolator()
                bounceAnim!!.addUpdateListener(this)
            }
        }

        fun startAnimation() {
            createAnimation()
            bounceAnim!!.start()
        }

        fun seek(seekTime: Long) {
            createAnimation()
            bounceAnim!!.currentPlayTime = seekTime
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
            canvas.translate(ball!!.x, ball!!.y)
            ball!!.shape!!.draw(canvas)
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
            val playtime = bounceAnim!!.currentPlayTime
            mSeekBar!!.progress = playtime.toInt()
        }

        override fun onAnimationCancel(animation: Animator) {}

        override fun onAnimationEnd(animation: Animator) {}

        override fun onAnimationRepeat(animation: Animator) {}

        override fun onAnimationStart(animation: Animator) {}
    }

    companion object {

        private val DURATION = 1500
        private val RED = -0x7f80
        private val BLUE = -0x7f7f01
        private val CYAN = -0x7f0001
        private val GREEN = -0x7f0080
        private val BALL_SIZE = 100f
    }
}