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
import android.graphics.Color
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.View
import ro.cojocar.dan.apianimations.R
import ro.cojocar.dan.apianimations.databinding.AnimationLoadingBinding
import java.util.*

/**
 * This application demonstrates loading Animator objects from
 * XML resources.
 */
class AnimationLoading : Activity() {
  private lateinit var binding: AnimationLoadingBinding

  /**
   * Called when the activity is first created.
   */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = AnimationLoadingBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    val container = binding.container
    val animView = MyAnimationView(this)
    container.addView(animView)

    val starter = binding.startButton
    starter.setOnClickListener { animView.startAnimation() }
  }

  inner class MyAnimationView(context: Context) : View(context),
    ValueAnimator.AnimatorUpdateListener {

    private val balls = ArrayList<ShapeHolder>()
    internal var animation: Animator? = null

    init {
      addBall(50f, 50f)
      addBall(200f, 50f)
      addBall(350f, 25f)
      addBall(500f, 25f, Color.GREEN)
      addBall(650f, 50f, Color.BLUE)
    }

    private fun createAnimation() {
      val appContext = this@AnimationLoading

      if (animation == null) {
        val anim =
          AnimatorInflater.loadAnimator(appContext, R.animator.object_animator) as ObjectAnimator
        anim.addUpdateListener(this)
        anim.target = balls[0]

        val fader = AnimatorInflater.loadAnimator(appContext, R.animator.animator) as ValueAnimator
        fader.addUpdateListener { animation -> balls[1].setAlpha(animation.animatedValue as Float) }

        val seq = AnimatorInflater.loadAnimator(
          appContext,
          R.animator.animator_set
        ) as AnimatorSet
        seq.setTarget(balls[2])

        val colorAnimator =
          AnimatorInflater.loadAnimator(appContext, R.animator.color_animator) as ObjectAnimator
        colorAnimator.target = balls[3]

        val colorAnimator2 =
          AnimatorInflater.loadAnimator(appContext, R.animator.color_animator) as ObjectAnimator
        colorAnimator2.target = balls[4]


        animation = AnimatorSet()
        (animation as AnimatorSet).playTogether(
          anim, fader, seq,
          colorAnimator, colorAnimator2
        )
      }
    }

    fun startAnimation() {
      createAnimation()
      animation!!.start()
    }

    private fun createBall(x: Float, y: Float): ShapeHolder {
      val circle = OvalShape()
      circle.resize(BALL_SIZE, BALL_SIZE)
      val drawable = ShapeDrawable(circle)
      val shapeHolder = ShapeHolder(drawable)
      shapeHolder.x = x
      shapeHolder.y = y
      return shapeHolder
    }

    private fun addBall(x: Float, y: Float, color: Int) {
      val shapeHolder = createBall(x, y)
      shapeHolder.color = color
      balls.add(shapeHolder)
    }

    private fun addBall(x: Float, y: Float) {
      val shapeHolder = createBall(x, y)
      val red = (100 + Math.random() * 155).toInt()
      val green = (100 + Math.random() * 155).toInt()
      val blue = (100 + Math.random() * 155).toInt()
      val color = -0x1000000 or (red shl 16) or (green shl 8) or blue
      val paint = shapeHolder.shape.paint
      val darkColor = -0x1000000 or (red / 4 shl 16) or (green / 4 shl 8) or blue / 4
      val gradient = RadialGradient(
        37.5f, 12.5f,
        50f, color, darkColor, Shader.TileMode.CLAMP
      )
      paint.shader = gradient
      balls.add(shapeHolder)
    }

    override fun onDraw(canvas: Canvas) {
      for (ball in balls) {
        canvas.translate(ball.x, ball.y)
        ball.shape.draw(canvas)
        canvas.translate(-ball.x, -ball.y)
      }
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {

      invalidate()
      val ball = balls[0]
      ball.y = animation.animatedValue as Float
    }
  }

  companion object {
    private const val BALL_SIZE = 100f
  }
}