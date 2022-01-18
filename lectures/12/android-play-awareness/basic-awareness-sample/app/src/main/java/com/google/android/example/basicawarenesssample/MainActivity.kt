/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.example.basicawarenesssample

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewbinding.BuildConfig
import com.example.android.common.logger.LogFragment
import com.google.android.example.basicawarenesssample.databinding.ActivityMainBinding
import com.google.android.gms.awareness.Awareness
import com.google.android.gms.awareness.fence.*
import com.google.android.gms.awareness.state.HeadphoneState

/**
 * Sample application which sets up a few context fences using the Awareness API, and takes
 * "snapshots" of data about the user and the device's surroundings.
 *
 * NOTE: for this sample to work, you need to add an API key in the manifest. See
 * https://developers.google.com/awareness/android-api/get-a-key for instructions.
 */
class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding

  private var mPendingIntent: PendingIntent? = null

  private var mFenceReceiver: FenceReceiver? = null

  private lateinit var mLogFragment: LogFragment

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.fab.setOnClickListener { printSnapshot() }

    val intent = Intent(FENCE_RECEIVER_ACTION)
    mPendingIntent = PendingIntent.getBroadcast(this@MainActivity, 0, intent, 0)

    mFenceReceiver = FenceReceiver()
    registerReceiver(mFenceReceiver, IntentFilter(FENCE_RECEIVER_ACTION))

    mLogFragment = supportFragmentManager.findFragmentById(R.id.log_fragment) as LogFragment
  }

  override fun onResume() {
    super.onResume()
    setupFences()
  }

  override fun onPause() {
    // Unregister the fence:
    Awareness.getFenceClient(this).updateFences(
      FenceUpdateRequest.Builder()
        .removeFence(FENCE_KEY)
        .build()
    )
      .addOnSuccessListener { Log.i(TAG, "Fence was successfully unregistered.") }
      .addOnFailureListener { e -> Log.e(TAG, "Fence could not be unregistered: $e") }

    super.onPause()
  }

  override fun onStop() {
    if (mFenceReceiver != null) {
      unregisterReceiver(mFenceReceiver)
      mFenceReceiver = null
    }
    super.onStop()
  }

  /**
   * Uses the snapshot API to print out some contextual information the device is "aware" of.
   */
  private fun printSnapshot() {
    // Clear the console screen of previous snapshot / fence log data
    mLogFragment.logView.text = ""

    // Each type of contextual information in the snapshot API has a corresponding "get" method.
    //  For instance, this is how to get the user's current Activity.
    Awareness.getSnapshotClient(this).detectedActivity
      .addOnSuccessListener { dar ->
        val arr = dar.activityRecognitionResult
        // getMostProbableActivity() is good enough for basic Activity detection.
        // To work within a threshold of confidence,
        // use ActivityRecognitionResult.getProbableActivities() to get a list of
        // potential current activities, and check the confidence of each one.
        val probableActivity = arr.mostProbableActivity

        val confidence = probableActivity.confidence
        val activityStr = probableActivity.toString()
        mLogFragment.logView.println("Activity: $activityStr, Confidence: $confidence/100")
      }

      .addOnFailureListener { e -> Log.e(TAG, "Could not detect activity: $e") }

    // Pulling headphone state is similar, but doesn't involve analyzing confidence.
    Awareness.getSnapshotClient(this).headphoneState
      .addOnSuccessListener { headphoneStateResponse ->
        val headphoneState = headphoneStateResponse.headphoneState
        val pluggedIn = headphoneState.state == HeadphoneState.PLUGGED_IN
        val stateStr = "Headphones are ${if (pluggedIn) "plugged in" else "unplugged"}"
        mLogFragment.logView.println(stateStr)
      }
      .addOnFailureListener { e -> Log.e(TAG, "Could not get headphone state: $e") }

    // Some of the data available via Snapshot API requires permissions that must be checked
    // at runtime.  Weather snapshots are a good example of this.  Since weather is protected
    // by a runtime permission, and permission request callbacks will happen asynchronously,
    // the easiest thing to do is put weather snapshot code in its own method.  That way it
    // can be called from here when permission has already been granted on subsequent runs,
    // and from the permission request callback code when permission is first granted.
    checkAndRequestWeatherPermissions()
  }

  /**
   * Helper method to retrieve weather data using the Snapshot API.  Since Weather is protected
   * by a runtime permission, this snapshot code is going to be called in multiple places:
   * [.printSnapshot] when the permission has already been accepted, and
   * [.onRequestPermissionsResult] when the permission is requested
   * and has been granted.
   */
  private fun getWeatherSnapshot() {
    if (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      Awareness.getSnapshotClient(this).weather
        .addOnSuccessListener { weatherResponse ->
          val weather = weatherResponse.weather
          weather.conditions
          mLogFragment.logView.println("Weather: $weather")
        }
        .addOnFailureListener { e -> Log.e(TAG, "Could not get weather: $e") }
    }
  }

  /**
   * Helper method to handle requesting the runtime permissions required for weather snapshots.
   *
   * @return true if the permission has already been granted, false otherwise.
   */
  private fun checkAndRequestWeatherPermissions() {
    val checkSelfPermission = ContextCompat.checkSelfPermission(
      this@MainActivity,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
    if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {

      if (ActivityCompat.shouldShowRequestPermissionRationale(
          this,
          Manifest.permission.ACCESS_FINE_LOCATION
        )
      ) {
        Log.i(
          TAG,
          "Permission previously denied and app shouldn't ask again.  Skipping weather snapshot."
        )
      } else {
        ActivityCompat.requestPermissions(
          this@MainActivity,
          arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
          MY_PERMISSION_LOCATION
        )
      }
    } else {
      getWeatherSnapshot()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      MY_PERMISSION_LOCATION -> {
        // If request is cancelled, the result arrays are empty.
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          getWeatherSnapshot()
        } else {
          Log.i(TAG, "Location permission denied.  Weather snapshot skipped.")
        }
      }
    }
  }

  /**
   * Sets up [AwarenessFence]'s for the sample app, and registers callbacks for them
   * with a custom [BroadcastReceiver]
   */
  private fun setupFences() {
    // DetectedActivityFence will fire when it detects the user performing the specified
    // activity.  In this case it's walking.
    val walkingFence = DetectedActivityFence.during(DetectedActivityFence.WALKING)

    // There are lots of cases where it's handy for the device to know if headphones have been
    // plugged in or unplugged.  For instance, if a music app detected your headphones fell out
    // when you were in a library, it'd be pretty considerate of the app to pause itself before
    // the user got in trouble.
    val headphoneFence = HeadphoneFence.during(HeadphoneState.PLUGGED_IN)

    // Combines multiple fences into a compound fence.  While the first two fences trigger
    // individually, this fence will only trigger its callback when all of its member fences
    // hit a true state.

//    val walkingWithHeadphones = AwarenessFence.and(walkingFence, headphoneFence)

    // We can even nest compound fences.  Using both "and" and "or" compound fences, this
    // compound fence will determine when the user has headphones in and is engaging in at least
    // one form of exercise.
    // The below breaks down to "(headphones plugged in) AND (walking OR running OR bicycling)"
    val exercisingWithHeadphonesFence = AwarenessFence.or(
      walkingFence,
      DetectedActivityFence.during(DetectedActivityFence.RUNNING),
//        DetectedActivityFence.during(DetectedActivityFence.STILL),
      DetectedActivityFence.during(DetectedActivityFence.ON_BICYCLE)
    )


    // Now that we have an interesting, complex condition, register the fence to receive
    // callbacks.

    // Register the fence to receive callbacks.
    Awareness.getFenceClient(this).updateFences(
      FenceUpdateRequest.Builder()
        .addFence(FENCE_KEY, exercisingWithHeadphonesFence, mPendingIntent!!)
        .build()
    )
      .addOnSuccessListener { Log.i(TAG, "Fence was successfully registered.") }
      .addOnFailureListener { e -> Log.e(TAG, "Fence could not be registered: $e") }
  }

  /**
   * A basic BroadcastReceiver to handle intents from from the Awareness API.
   */
  inner class FenceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
      if (!TextUtils.equals(FENCE_RECEIVER_ACTION, intent.action)) {
        mLogFragment.logView
          .println("Received an unsupported action in FenceReceiver: action=" + intent.action!!)
        return
      }

      // The state information for the given fence is em
      val fenceState = FenceState.extract(intent)

      if (TextUtils.equals(fenceState.fenceKey, FENCE_KEY)) {
        val fenceStateStr: String = when (fenceState.currentState) {
          FenceState.TRUE -> "true"
          FenceState.FALSE -> "false"
          FenceState.UNKNOWN -> "unknown"
          else -> "unknown value"
        }
        mLogFragment.logView.println("Fence state: $fenceStateStr")
      }
    }
  }

  companion object {
    // The intent action which will be fired when your fence is triggered.
    private const val FENCE_RECEIVER_ACTION =
      "${BuildConfig.LIBRARY_PACKAGE_NAME} FENCE_RECEIVER_ACTION"

    private const val TAG = "MainActivity"

    // The fence key is how callback code determines which fence fired.
    private const val FENCE_KEY = "fence_key"

    private const val MY_PERMISSION_LOCATION = 1
  }
}