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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import android.view.*
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

  private lateinit var mSimulationView: SimulationView
  private lateinit var mWakeLock: WakeLock

  /** Called when the activity is first created.  */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Get an instance of the SensorManager
    val mSensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Get an instance of the PowerManager
    val mPowerManager: PowerManager = getSystemService(Context.POWER_SERVICE) as PowerManager

    // Create a bright wake lock
    mWakeLock =
      mPowerManager.newWakeLock(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, javaClass.name)

    // instantiate our simulation view and set it as the activity's content
    mSimulationView = SimulationView(this)
    mSimulationView.setSensorManager(mSensorManager)
    mSimulationView.setBackgroundResource(R.drawable.wood)
    setContentView(mSimulationView)
  }

  override fun onResume() {
    super.onResume()
    /*
     * when the activity is resumed, we acquire a wake-lock so that the
     * screen stays on, since the user will likely not be fiddling with the
     * screen or buttons.
     */
    mWakeLock.acquire(10 * TimeUnit.MINUTES.toMillis(1))

    // Start the simulation
    mSimulationView.startSimulation()
  }

  override fun onPause() {
    super.onPause()
    /*
     * When the activity is paused, we make sure to stop the simulation,
     * release our sensor resources and wake locks
     */
    // Stop the simulation
    mSimulationView.stopSimulation()

    // and release our wake-lock
    mWakeLock.release()
  }
}