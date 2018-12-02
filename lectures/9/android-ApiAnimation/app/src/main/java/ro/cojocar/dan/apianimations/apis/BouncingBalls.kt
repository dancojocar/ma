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
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import ro.cojocar.dan.apianimations.R
import java.util.*


class BouncingBalls : Activity() {
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bouncing_balls)
        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(MyAnimationView(this))
    }

    inner class MyAnimationView(context: Context) : View(context) {

        val balls = ArrayList<ShapeHolder>()
        internal var animation: AnimatorSet? = null

        init {

            // Animate background color
            // Note that setting the background color will automatically
            // invalidate the view, so that the animated color, and the
            // bouncing balls, get redisplayed on every frame of the animation.
            val colorAnim = ObjectAnimator.ofInt(
                this,
                "backgroundColor", RED, BLUE
            )
            colorAnim.setDuration(3000)
            colorAnim.setEvaluator(ArgbEvaluator())
            colorAnim.setRepeatCount(ValueAnimator.INFINITE)
            colorAnim.setRepeatMode(ValueAnimator.REVERSE)
            colorAnim.start()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_MOVE) {
                return false
            }

            val newBall = addBall(event.x, event.y)

            // Bouncing animation with squash and stretch
            val startY = newBall.y
            val endY = height - 50f
            val h = height.toFloat()
            val eventY = event.y
            val duration = (500 * ((h - eventY) / h)).toInt()
            val bounceAnim = ObjectAnimator.ofFloat(
                newBall,
                "y", startY, endY
            )
            bounceAnim.duration = duration.toLong()
            bounceAnim.interpolator = AccelerateInterpolator()
            val squashAnim1 = ObjectAnimator.ofFloat(
                newBall,
                "x", newBall.x,
                newBall.x - 25f
            )
            squashAnim1.duration = (duration / 4).toLong()
            squashAnim1.repeatCount = 1
            squashAnim1.repeatMode = ValueAnimator.REVERSE
            squashAnim1.interpolator = DecelerateInterpolator()
            val squashAnim2 = ObjectAnimator.ofFloat(
                newBall,
                "width", newBall.width,
                newBall.width + 50
            )
            squashAnim2.duration = (duration / 4).toLong()
            squashAnim2.repeatCount = 1
            squashAnim2.repeatMode = ValueAnimator.REVERSE
            squashAnim2.interpolator = DecelerateInterpolator()
            val stretchAnim1 = ObjectAnimator.ofFloat(
                newBall,
                "y", endY,
                endY + 25f
            )
            stretchAnim1.duration = (duration / 4).toLong()
            stretchAnim1.repeatCount = 1
            stretchAnim1.interpolator = DecelerateInterpolator()
            stretchAnim1.repeatMode = ValueAnimator.REVERSE
            val stretchAnim2 = ObjectAnimator.ofFloat(
                newBall,
                "height",
                newBall.height, newBall.height - 25
            )
            stretchAnim2.duration = (duration / 4).toLong()
            stretchAnim2.repeatCount = 1
            stretchAnim2.interpolator = DecelerateInterpolator()
            stretchAnim2.repeatMode = ValueAnimator.REVERSE
            val bounceBackAnim = ObjectAnimator.ofFloat(
                newBall,
                "y", endY, startY
            )
            bounceBackAnim.duration = duration.toLong()
            bounceBackAnim.interpolator = DecelerateInterpolator()
            // Sequence the down/squash&stretch/up animations
            val bouncer = AnimatorSet()
            bouncer.play(bounceAnim).before(squashAnim1)
            bouncer.play(squashAnim1).with(squashAnim2)
            bouncer.play(squashAnim1).with(stretchAnim1)
            bouncer.play(squashAnim1).with(stretchAnim2)
            bouncer.play(bounceBackAnim).after(stretchAnim1)

            // Fading animation - remove the ball when the animation is done
            val fadeAnim = ObjectAnimator.ofFloat(
                newBall,
                "alpha", 1f, 0f
            )
            fadeAnim.duration = 250
            fadeAnim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val target = (animation as ObjectAnimator).target as ShapeHolder
                    balls.remove(target)
                }
            })

            // Sequence the two animations to play one after the other
            val animatorSet = AnimatorSet()
            animatorSet.play(bouncer).before(fadeAnim)

            // Start the animation
            animatorSet.start()

            return true
        }

        private fun addBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(50f, 50f)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x - 25f
            shapeHolder.y = y - 25f
            val red = (Math.random() * 255).toInt()
            val green = (Math.random() * 255).toInt()
            val blue = (Math.random() * 255).toInt()
            val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
            val paint = drawable.paint //new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.color = color
            shapeHolder.paint = paint
            balls.add(shapeHolder)
            return shapeHolder
        }

        override fun onDraw(canvas: Canvas) {
            for (i in balls.indices) {
                val shapeHolder = balls[i]
                canvas.save()
                canvas.translate(shapeHolder.x, shapeHolder.y)
                shapeHolder.shape!!.draw(canvas)
                canvas.restore()
            }
        }

    }

    companion object {
        private const val RED = -0x7f80
        private const val BLUE = -0x7f7f01
    }
}