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
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import ro.cojocar.dan.apianimations.R
import java.util.*


class AnimationCloning : Activity() {
    /**
     * Called when the activity is first created.
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.animation_cloning)
        val container = findViewById<LinearLayout>(R.id.container)
        val animView = MyAnimationView(this)
        container.addView(animView)

        val starter = findViewById<Button>(R.id.startButton)
        starter.setOnClickListener { animView.startAnimation() }
    }

    inner class MyAnimationView(context: Context) : View(context), ValueAnimator.AnimatorUpdateListener {

        val balls = ArrayList<ShapeHolder>()
        internal var animation: AnimatorSet? = null
        private val mDensity: Float

        init {

            mDensity = getContext().resources.displayMetrics.density

            balls.add(addBall(50f, 25f))
            balls.add(addBall(150f, 25f))
            balls.add(addBall(250f, 50f))
            balls.add(addBall(350f, 50f))
        }

        private fun createAnimation() {
            if (animation == null) {
                val anim1 = ObjectAnimator.ofFloat(
                    balls[0],
                    "y", 0f, height - balls[0].height
                ).setDuration(2500)
                val anim2 = anim1.clone()
                anim2.target = balls[1]
                anim1.addUpdateListener(this)

                val ball2 = balls[2]
                val animDown = ObjectAnimator.ofFloat(
                    ball2,
                    "y", 0f, height - ball2.height
                ).setDuration(500)
                animDown.interpolator = AccelerateInterpolator()
                val animUp = ObjectAnimator.ofFloat(
                    ball2,
                    "y",
                    height - ball2.height, 0f
                ).setDuration(500)
                animUp.interpolator = DecelerateInterpolator()
                val s1 = AnimatorSet()
                s1.playSequentially(animDown, animUp)
                animDown.addUpdateListener(this)
                animUp.addUpdateListener(this)
                val s2 = s1.clone()
                s2.setTarget(balls[3])

                animation = AnimatorSet()
                animation!!.playTogether(anim1, anim2, s1)
                animation!!.playSequentially(s1, s2)
            }
        }

        private fun addBall(x: Float, y: Float): ShapeHolder {
            val circle = OvalShape()
            circle.resize(50f * mDensity, 50f * mDensity)
            val drawable = ShapeDrawable(circle)
            val shapeHolder = ShapeHolder(drawable)
            shapeHolder.x = x - 25f
            shapeHolder.y = y - 25f
            val red = (100 + Math.random() * 155).toInt()
            val green = (100 + Math.random() * 155).toInt()
            val blue = (100 + Math.random() * 155).toInt()
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
            for (i in balls.indices) {
                val shapeHolder = balls[i]
                canvas.save()
                canvas.translate(shapeHolder.x, shapeHolder.y)
                shapeHolder.shape!!.draw(canvas)
                canvas.restore()
            }
        }

        fun startAnimation() {
            createAnimation()
            animation!!.start()
        }

        override fun onAnimationUpdate(animation: ValueAnimator) {
            invalidate()
        }

    }
}