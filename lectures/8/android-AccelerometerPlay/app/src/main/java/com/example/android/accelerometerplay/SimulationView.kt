package com.example.android.accelerometerplay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.ViewGroup
import android.widget.FrameLayout

class SimulationView(context: Context) : FrameLayout(context), SensorEventListener {

  companion object {
    // diameter of the balls in meters
    const val sBallDiameter = 0.008f
  }

  private lateinit var mAccelerometer: Sensor
  private lateinit var mSensorManager: SensorManager
  private val mDstWidth: Int
  private val mDstHeight: Int


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

  init {

    val metrics = resources.displayMetrics
    mXDpi = metrics.xdpi
    mYDpi = metrics.ydpi
    mMetersToPixelsX = mXDpi / 0.0254f
    mMetersToPixelsY = mYDpi / 0.0254f

    // rescale the ball so it's about 0.5 cm on screen
    mDstWidth = (sBallDiameter * mMetersToPixelsX + 0.5f).toInt()
    mDstHeight = (sBallDiameter * mMetersToPixelsY + 0.5f).toInt()
    mParticleSystem = ParticleSystem(context)

    for (ball in mParticleSystem.mBalls) {
      addView(ball, ViewGroup.LayoutParams(mDstWidth, mDstHeight))
    }

    val opts = BitmapFactory.Options()
//      opts.inDither = true
    opts.inPreferredConfig = Bitmap.Config.RGB_565
  }

  fun startSimulation() {
    /*
     * It is not necessary to get accelerometer events at a very high
     * rate, by using a slower rate (SENSOR_DELAY_UI), we get an
     * automatic low-pass filter, which "extracts" the gravity component
     * of the acceleration. As an added benefit, we use less power and
     * CPU resources.
     */
    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME)
  }

  fun stopSimulation() {
    mSensorManager.unregisterListener(this)
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
    when (display?.rotation) {
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

    particleSystem.update(sx, sy, now, mHorizontalBound, mVerticalBound)

    val xc = mXOrigin
    val yc = mYOrigin
    val xs = mMetersToPixelsX
    val ys = mMetersToPixelsY
    for (ball in particleSystem.mBalls) {
      /*
       * We transform the canvas so that the coordinate system matches
       * the sensors coordinate system with the origin in the center
       * of the screen, and the unit is the meter.
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

  fun setSensorManager(mSensorManager: SensorManager) {
    this.mSensorManager = mSensorManager
    this.mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
  }
}

