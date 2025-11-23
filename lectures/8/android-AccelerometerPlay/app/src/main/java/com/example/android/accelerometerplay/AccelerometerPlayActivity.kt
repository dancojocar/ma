package com.example.android.accelerometerplay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.android.accelerometerplay.R
import com.example.android.accelerometerplay.SimulationView
import androidx.core.view.WindowCompat
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
class AccelerometerPlayActivity : ComponentActivity() {

  private lateinit var mSimulationView: SimulationView
  private lateinit var mWakeLock: PowerManager.WakeLock

  /** Called when the activity is first created.  */
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)

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

    setContent {
      AccelerometerPlayApp(mSimulationView)
    }
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

@Composable
fun AccelerometerPlayApp(simulationView: SimulationView) {
  Box(
    modifier = Modifier
      .fillMaxSize()
  ) {
    Image(
      painter = painterResource(id = R.drawable.wood),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.FillBounds
    )

    AndroidView(
      factory = { simulationView },
      modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()
    )
  }
}