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
    private const val NUM_MAX_ITERATIONS = 10
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
            var dx = ball.mPosX - currBall.mPosX
            var dy = ball.mPosY - currBall.mPosY
            var dd = dx * dx + dy * dy
            // Check for collisions
            if (dd < sBallDiameter2) {
              /*
             * add a little entropy, after nothing is
             * perfect in the universe.
             */
              dx += (Math.random().toFloat() - 0.5f) * 0.0001f
              dy += (Math.random().toFloat() - 0.5f) * 0.0001f
              dd = dx * dx + dy * dy
              // simulate the spring
              val d = sqrt(dd.toDouble()).toFloat()
              val c = 0.5f * (sBallDiameter - d) / d
              val effectX = dx * c
              val effectY = dy * c
              currBall.mPosX -= effectX
              currBall.mPosY -= effectY
              ball.mPosX += effectX
              ball.mPosY += effectY
              more = true
            }
          }
        }
        currBall.resolveCollisionWithBounds(xMax, yMax)
      }
      k++
    }
  }
}