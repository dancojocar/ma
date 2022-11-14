package com.example.android.accelerometerplay

import android.content.Context
import android.util.AttributeSet
import android.view.View

/*
   * Each of our particle holds its previous and current position, its
   * acceleration. for added realism each particle has its own friction
   * coefficient.
   */
class Particle : View {
  var mPosX = Math.random().toFloat()
  var mPosY = Math.random().toFloat()
  private var mVelX: Float = 0.toFloat()
  private var mVelY: Float = 0.toFloat()

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

  constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  )

  fun computePhysics(sx: Float, sy: Float, dT: Float) {

    val ax = -sx / 5
    val ay = -sy / 5

    mPosX += mVelX * dT + ax * dT * dT / 2
    mPosY += mVelY * dT + ay * dT * dT / 2

    mVelX += ax * dT
    mVelY += ay * dT
  }

  /*
   * Resolving constraints and collisions with the Verlet integrator
   * can be very simple, we simply need to move a colliding or
   * constrained particle in such way that the constraint is
   * satisfied.
   */
  fun resolveCollisionWithBounds(xMax: Float, yMax: Float) {
    val x = mPosX
    val y = mPosY
    if (x > xMax) {
      mPosX = xMax
      mVelX = 0f
    } else if (x < -xMax) {
      mPosX = -xMax
      mVelX = 0f
    }
    if (y > yMax) {
      mPosY = yMax
      mVelY = 0f
    } else if (y < -yMax) {
      mPosY = -yMax
      mVelY = 0f
    }
  }
}