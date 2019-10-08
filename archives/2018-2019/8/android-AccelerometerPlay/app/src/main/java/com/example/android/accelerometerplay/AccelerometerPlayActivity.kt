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

package com.example.android.accelerometerplay

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory.Options
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.*
import android.widget.FrameLayout
import java.util.concurrent.TimeUnit

/**
 * This is an example of using the accelerometer to integrate the device's
 * acceleration to a position using the Verlet method. This is illustrated with
 * a very simple particle system comprised of a few iron balls freely moving on
 * an inclined wooden table. The inclination of the virtual table is controlled
 * by the device's accelerometer.
 *
 * @see SensorManager
 *
 * @see SensorEvent
 *
 * @see Sensor
 */
class AccelerometerPlayActivity : Activity() {

  private var mSimulationView: SimulationView? = null
  private var mSensorManager: SensorManager? = null
  private var mPowerManager: PowerManager? = null
  private var mWindowManager: WindowManager? = null
  private var mDisplay: Display? = null
  private var mWakeLock: WakeLock? = null

  /** Called when the activity is first created.  */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Get an instance of the SensorManager
    mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Get an instance of the PowerManager
    mPowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

    // Get an instance of the WindowManager
    mWindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    mDisplay = mWindowManager!!.defaultDisplay

    // Create a bright wake lock
    mWakeLock = mPowerManager!!.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, javaClass.name)

    // instantiate our simulation view and set it as the activity's content
    mSimulationView = SimulationView(this)
    mSimulationView!!.setBackgroundResource(R.drawable.wood)
    setContentView(mSimulationView)
  }

  override fun onResume() {
    super.onResume()
    /*
     * when the activity is resumed, we acquire a wake-lock so that the
     * screen stays on, since the user will likely not be fiddling with the
     * screen or buttons.
     */
    mWakeLock!!.acquire(10 * TimeUnit.MINUTES.toMillis(1))

    // Start the simulation
    mSimulationView!!.startSimulation()
  }

  override fun onPause() {
    super.onPause()
    /*
     * When the activity is paused, we make sure to stop the simulation,
     * release our sensor resources and wake locks
     */
    // Stop the simulation
    mSimulationView!!.stopSimulation()

    // and release our wake-lock
    mWakeLock!!.release()
  }

  internal inner class SimulationView(context: Context) : FrameLayout(context), SensorEventListener {

    private val mDstWidth: Int
    private val mDstHeight: Int

    private val mAccelerometer: Sensor = mSensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private var mLastT: Long = 0

    private val mXDpi: Float
    private val mYDpi: Float
    private val mMetersToPixelsX: Float
    private val mMetersToPixelsY: Float
    private var mXOrigin: Float = 0.toFloat()
    private var mYOrigin: Float = 0.toFloat()
    private var mSensorX: Float = 0.toFloat()
    private var mSensorY: Float = 0.toFloat()
    private var mHorizontalBound: Float = 0.toFloat()
    private var mVerticalBound: Float = 0.toFloat()
    private val mParticleSystem: ParticleSystem

    /*
     * Each of our particle holds its previous and current position, its
     * acceleration. for added realism each particle has its own friction
     * coefficient.
     */
    internal inner class Particle : View {
      var mPosX = Math.random().toFloat()
      var mPosY = Math.random().toFloat()
      private var mVelX: Float = 0.toFloat()
      private var mVelY: Float = 0.toFloat()

      constructor(context: Context) : super(context)

      constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

      constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

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
      fun resolveCollisionWithBounds() {
        val xmax = mHorizontalBound
        val ymax = mVerticalBound
        val x = mPosX
        val y = mPosY
        if (x > xmax) {
          mPosX = xmax
          mVelX = 0f
        } else if (x < -xmax) {
          mPosX = -xmax
          mVelX = 0f
        }
        if (y > ymax) {
          mPosY = ymax
          mVelY = 0f
        } else if (y < -ymax) {
          mPosY = -ymax
          mVelY = 0f
        }
      }
    }

    /*
     * A particle system is just a collection of particles
     */
    internal inner class ParticleSystem {
      val mBalls = mutableListOf<Particle>()

      init {
        /*
         * Initially our particles have no speed or acceleration
         */
        for (i in 0..NUM_PARTICLES) {
          val ball = Particle(context)
          ball.setBackgroundResource(R.drawable.ball)
          ball.setLayerType(View.LAYER_TYPE_HARDWARE, null)
          mBalls.add(ball)
          addView(ball, ViewGroup.LayoutParams(mDstWidth, mDstHeight))
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
      fun update(sx: Float, sy: Float, now: Long) {
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
            for (j in i + 1 until count) {
              val ball = mBalls[j]
              var dx = ball.mPosX - currBall.mPosX
              var dy = ball.mPosY - currBall.mPosY
              var dd = dx * dx + dy * dy
              // Check for collisions
              if (dd < sBallDiameter2) {
                /*
                 * add a little bit of entropy, after nothing is
                 * perfect in the universe.
                 */
                dx += (Math.random().toFloat() - 0.5f) * 0.0001f
                dy += (Math.random().toFloat() - 0.5f) * 0.0001f
                dd = dx * dx + dy * dy
//                dd = sBallDiameter2
                // simulate the spring
                val d = Math.sqrt(dd.toDouble()).toFloat()
                val c = 0.5f * (sBallDiameter - d) / d
                val effectX = dx * c
                val effectY = dy * c
//                currBall.mPosX -= effectX
//                currBall.mPosY -= effectY
                ball.mPosX += effectX
                ball.mPosY += effectY
                more = true
              }
            }
            currBall.resolveCollisionWithBounds()
          }
          k++
        }
      }
    }

    fun startSimulation() {
      /*
       * It is not necessary to get accelerometer events at a very high
       * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
       * automatic low-pass filter, which "extracts" the gravity component
       * of the acceleration. As an added benefit, we use less power and
       * CPU resources.
       */
      mSensorManager!!.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stopSimulation() {
      mSensorManager!!.unregisterListener(this)
    }

    init {

      val metrics = DisplayMetrics()
      windowManager.defaultDisplay.getMetrics(metrics)
      mXDpi = metrics.xdpi
      mYDpi = metrics.ydpi
      mMetersToPixelsX = mXDpi / 0.0254f
      mMetersToPixelsY = mYDpi / 0.0254f

      // rescale the ball so it's about 0.5 cm on screen
      mDstWidth = (sBallDiameter * mMetersToPixelsX + 0.5f).toInt()
      mDstHeight = (sBallDiameter * mMetersToPixelsY + 0.5f).toInt()
      mParticleSystem = ParticleSystem()

      val opts = Options()
//      opts.inDither = true
      opts.inPreferredConfig = Bitmap.Config.RGB_565
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
      // compute the origin of the screen relative to the origin of
      // the bitmap
      mXOrigin = (w - mDstWidth) * 0.5f
      mYOrigin = (h - mDstHeight) * 0.5f
      mHorizontalBound = (w / mMetersToPixelsX - sBallDiameter) * 0.5f
      mVerticalBound = (h / mMetersToPixelsY - sBallDiameter) * 0.5f
    }

    override fun onSensorChanged(event: SensorEvent) {
      if (event.sensor.type != Sensor.TYPE_ACCELEROMETER)
        return
      /*
       * record the accelerometer data, the event's timestamp as well as
       * the current time. The latter is needed so we can calculate the
       * "present" time during rendering. In this application, we need to
       * take into account how the screen is rotated with respect to the
       * sensors (which always return data in a coordinate space aligned
       * to with the screen in its native orientation).
       */
      when (mDisplay!!.rotation) {
        Surface.ROTATION_0 -> {
          mSensorX = event.values[0]
          mSensorY = event.values[1]
        }
        Surface.ROTATION_90 -> {
          mSensorX = -event.values[1]
          mSensorY = event.values[0]
        }
        Surface.ROTATION_180 -> {
          mSensorX = -event.values[0]
          mSensorY = -event.values[1]
        }
        Surface.ROTATION_270 -> {
          mSensorX = event.values[1]
          mSensorY = -event.values[0]
        }
      }
    }

    override fun onDraw(canvas: Canvas) {
      /*
       * Compute the new position of our object, based on accelerometer
       * data and present time.
       */
      val particleSystem = mParticleSystem
      val now = System.currentTimeMillis()
      val sx = mSensorX
      val sy = mSensorY

      particleSystem.update(sx, sy, now)

      val xc = mXOrigin
      val yc = mYOrigin
      val xs = mMetersToPixelsX
      val ys = mMetersToPixelsY
      for (ball in particleSystem.mBalls) {
        /*
         * We transform the canvas so that the coordinate system matches
         * the sensors coordinate system with the origin in the center
         * of the screen and the unit is the meter.
         */
        val x = xc + ball.mPosX * xs
        val y = yc - ball.mPosY * ys
        ball.translationX = x
        ball.translationY = y
      }

      // and make sure to redraw asap
      invalidate()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

  }

  companion object {
    const val NUM_PARTICLES = 5
    // diameter of the balls in meters
    private const val sBallDiameter = 0.008f
    private const val sBallDiameter2 = sBallDiameter * sBallDiameter
    private const val NUM_MAX_ITERATIONS = 10
  }
}
