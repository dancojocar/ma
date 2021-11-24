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
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat
import ro.cojocar.dan.apianimations.databinding.AnimatorEventsBinding

/**
 * This demo shows how the AnimatorListener events work.
 */
class AnimatorEvents : Activity() {
  private lateinit var binding: AnimatorEventsBinding

  lateinit var startText: TextView
  lateinit var repeatText: TextView
  lateinit var cancelText: TextView
  lateinit var endText: TextView
  lateinit var startTextAnimator: TextView
  lateinit var repeatTextAnimator: TextView
  lateinit var cancelTextAnimator: TextView
  lateinit var endTextAnimator: TextView

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = AnimatorEventsBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    val container = binding.container
    val animView = MyAnimationView(this)
    container.addView(animView)
    startText = binding.startText
    startText.alpha = .5f
    repeatText = binding.repeatText
    repeatText.alpha = .5f
    cancelText = binding.cancelText
    cancelText.alpha = .5f
    endText = binding.endText
    endText.alpha = .5f
    startTextAnimator = binding.startTextAnimator
    startTextAnimator.alpha = .5f
    repeatTextAnimator = binding.repeatTextAnimator
    repeatTextAnimator.alpha = .5f
    cancelTextAnimator = binding.cancelTextAnimator
    cancelTextAnimator.alpha = .5f
    endTextAnimator = binding.endTextAnimator
    endTextAnimator.alpha = .5f
    val endCB = binding.endCB


    val starter = binding.startButton
    starter.setOnClickListener { animView.startAnimation(endCB.isChecked) }

    val canceler = binding.cancelButton
    canceler.setOnClickListener { animView.cancelAnimation() }

    val ender = binding.endButton
    ender.setOnClickListener { animView.endAnimation() }

  }

  inner class MyAnimationView(context: Context) : View(context), Animator.AnimatorListener,
    ValueAnimator.AnimatorUpdateListener {

    internal var animation: Animator? = null
    private var ball: ShapeHolder = createBall()
    private var endImmediately = false

    private fun createAnimation() {
      if (animation == null) {
        val yAnim = ObjectAnimator.ofFloat(
          ball, "y",
          ball.y, height - 50f
        ).setDuration(1500)
        yAnim.repeatCount = 2
        yAnim.repeatMode = ValueAnimator.REVERSE
        yAnim.interpolator = AccelerateInterpolator(2f)
        yAnim.addUpdateListener(this)
        yAnim.addListener(this)

        val xAnim = ObjectAnimator.ofFloat(
          ball, "x",
          ball.x, ball.x + width / 2
        ).setDuration(1000)
        xAnim.startDelay = 0
        xAnim.repeatCount = 2
        xAnim.repeatMode = ValueAnimator.REVERSE
        xAnim.interpolator = AccelerateInterpolator(2f)

        val alphaAnim = ObjectAnimator.ofFloat(ball, "alpha", 1f, 0f, 1f).setDuration(1500)
        alphaAnim.start()

        animation = AnimatorSet()
        (animation as AnimatorSet).playTogether(yAnim, xAnim)
        animation!!.addListener(this)
      }
    }

    fun startAnimation(endImmediately: Boolean) {
      this.endImmediately = endImmediately
      startText.alpha = .5f
      repeatText.alpha = .5f
      cancelText.alpha = .5f
      endText.alpha = .5f
      startTextAnimator.alpha = .5f
      repeatTextAnimator.alpha = .5f
      cancelTextAnimator.alpha = .5f
      endTextAnimator.alpha = .5f
      val gray = ContextCompat.getColor(context, android.R.color.darker_gray)
      startText.setTextColor(gray)
      repeatText.setTextColor(gray)
      cancelText.setTextColor(gray)
      endText.setTextColor(gray)
      startTextAnimator.setTextColor(gray)
      repeatTextAnimator.setTextColor(gray)
      cancelTextAnimator.setTextColor(gray)
      endTextAnimator.setTextColor(gray)
      createAnimation()
      animation!!.start()
    }

    fun cancelAnimation() {
      createAnimation()
      animation!!.cancel()
    }

    fun endAnimation() {
      createAnimation()
      animation!!.end()
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
      canvas.translate(ball.x, ball.y)
      ball.shape.draw(canvas)
      canvas.restore()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
      invalidate()
    }

    override fun onAnimationStart(animation: Animator) {
      val color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
      if (animation is AnimatorSet) {
        startText.alpha = 1f
        startText.setTextColor(color)
      } else {
        startTextAnimator.alpha = 1f
        startTextAnimator.setTextColor(color)
      }
      if (endImmediately) {
        animation.end()
      }
    }

    override fun onAnimationEnd(animation: Animator) {
      val color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
      if (animation is AnimatorSet) {
        endText.alpha = 1f
        endText.setTextColor(color)
      } else {
        endTextAnimator.alpha = 1f
        endTextAnimator.setTextColor(color)
      }
    }

    override fun onAnimationCancel(animation: Animator) {
      val color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
      if (animation is AnimatorSet) {
        cancelText.alpha = 1f
        cancelText.setTextColor(color)
      } else {
        cancelTextAnimator.alpha = 1f
        cancelTextAnimator.setTextColor(color)
      }
    }

    override fun onAnimationRepeat(animation: Animator) {
      val color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
      if (animation is AnimatorSet) {
        repeatText.alpha = 1f
        repeatText.setTextColor(color)
      } else {
        repeatTextAnimator.alpha = 1f
        repeatTextAnimator.setTextColor(color)
      }
    }
  }
}