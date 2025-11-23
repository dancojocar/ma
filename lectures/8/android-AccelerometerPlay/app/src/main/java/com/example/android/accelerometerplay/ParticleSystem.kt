package com.example.android.accelerometerplay

import android.content.Context
import android.view.View
import kotlin.math.sqrt

/*
 * A particle system is just a collection of particles
 */
class ParticleSystem(context: Context) {
  private var mLastT: Long = 0
  val mBalls = mutableListOf<Particle>()

  companion object {
    const val NUM_PARTICLES = 5

    // diameter of the balls in meters
    private const val sBallDiameter = SimulationView.sBallDiameter
    private const val sBallDiameter2 = sBallDiameter * sBallDiameter

    // Reasonable number of iterations to resolve overlaps without excessive chattering
    private const val NUM_MAX_ITERATIONS = 6

    // Fixed minimum penetration depth (in meters) required before we resolve a collision
    private const val MIN_PENETRATION_FOR_RESOLVE = 0.05f * sBallDiameter
  }



  init {
    /*
     * Initially our particles have no speed or acceleration
     */
    for (i in 0..NUM_PARTICLES) {
      val ball = Particle(context)
      ball.setBackgroundResource(R.drawable.ball)
      ball.setLayerType(View.LAYER_TYPE_HARDWARE, null)
      mBalls.add(ball)

    }
  }

  /*
   * Update the position of each particle in the system using the
   * Verlet integrator.
   */
  private fun updatePositions(sx: Float, sy: Float, timestamp: Long) {
    if (mLastT != 0L) {
      val dT = (timestamp - mLastT).toFloat() / 1000f
      /** (1.0f / 1000000000.0f) */
      for (ball in mBalls) {
        ball.computePhysics(sx, sy, dT)
      }
    }
    mLastT = timestamp
  }

  /*
   * Performs one iteration of the simulation. First updating the
   * position of all the particles and resolving the constraints and
   * collisions.
   */
  fun update(sx: Float, sy: Float, now: Long, xMax: Float, yMax: Float) {
    // update the system's positions
    updatePositions(sx, sy, now)

    /*
     * Resolve collisions, each particle is tested against every
     * other particle for collision. If a collision is detected the
     * particle is moved away using a virtual spring of infinite
     * stiffness.
     */
    var more = true
    val count = mBalls.size
    var k = 0
    while (k < NUM_MAX_ITERATIONS && more) {
      more = false
      for (i in 0 until count) {
        val currBall = mBalls[i]
        for (j in 0 until count) {
          if (i != j) {
            val ball = mBalls[j]
            val dx = ball.mPosX - currBall.mPosX
            val dy = ball.mPosY - currBall.mPosY
            val dd = dx * dx + dy * dy
            // Check for collisions
            if (dd < sBallDiameter2 && dd > 0f) {
              val d = sqrt(dd.toDouble()).toFloat()
              val penetration = sBallDiameter - d
              if (penetration > MIN_PENETRATION_FOR_RESOLVE) {
                val c = 0.5f * penetration / d
                val effectX = dx * c
                val effectY = dy * c
                currBall.mPosX -= effectX
                currBall.mPosY -= effectY
                ball.mPosX += effectX
                ball.mPosY += effectY

                // Damp velocities on collision so balls slow down and settle
                currBall.dampenVelocityOnCollision()
                ball.dampenVelocityOnCollision()

                more = true
              }
            }
          }
        }
        currBall.resolveCollisionWithBounds(xMax, yMax)
      }
      k++
    }
  }
}