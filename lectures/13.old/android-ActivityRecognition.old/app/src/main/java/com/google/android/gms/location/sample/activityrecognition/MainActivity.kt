/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.location.sample.activityrecognition

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import androidx.viewbinding.BuildConfig
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.sample.activityrecognition.databinding.MainActivityBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import java.util.*

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {
    private lateinit var binding: MainActivityBinding

    /**
     * The entry point for interacting with activity recognition.
     */
    private lateinit var mActivityRecognitionClient: ActivityRecognitionClient

    /**
     * Adapter backed by a list of DetectedActivity objects.
     */
    private lateinit var mAdapter: DetectedActivitiesAdapter

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // Enable either the Request Updates button or the Remove Updates button depending on
        // whether activity updates have been requested.
        setButtonsEnabledState()
        val detectedActivities = Utils.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(this).getString(
                        Constants.KEY_DETECTED_ACTIVITIES, ""
                )
        )
        // Bind the adapter to the ListView responsible for display data for detected activities.
        mAdapter = DetectedActivitiesAdapter(this, detectedActivities)
        binding.detectedActivitiesListview.adapter = mAdapter
        mActivityRecognitionClient = ActivityRecognitionClient(this)
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this)
        updateDetectedActivitiesList()
    }

    override fun onPause() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    /**
     * Registers for activity recognition updates using
     * [ActivityRecognitionClient.requestActivityUpdates].
     * Registers success and failure callbacks.
     */
    fun requestActivityUpdatesButtonHandler(view: View?) {
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            requestUpdate()
        }
    }

    private fun requestUpdate() {
        val task = mActivityRecognitionClient.requestActivityUpdates(
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                activityDetectionPendingIntent
        )
        task.addOnSuccessListener {
            Toast.makeText(
                    this,
                    getString(R.string.activity_updates_enabled),
                    Toast.LENGTH_SHORT
            )
                    .show()
            updatesRequestedState = true
            updateDetectedActivitiesList()
        }
        task.addOnFailureListener { e ->
            logw(getString(R.string.activity_updates_not_enabled), e)
            Toast.makeText(
                    this,
                    getString(R.string.activity_updates_not_enabled),
                    Toast.LENGTH_SHORT
            )
                    .show()
            updatesRequestedState = false
        }
    }

    private fun checkPermissions() =
            ActivityCompat.checkSelfPermission(this, ACTIVITY_RECOGNITION) == PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
                this, arrayOf(ACTIVITY_RECOGNITION),
                REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACTIVITY_RECOGNITION)) {
            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            logi("Displaying permission rationale to provide additional context.")
            showSnackbar(R.string.permission_rationale, android.R.string.ok) {
                // Request permission
                startLocationPermissionRequest()
            }

        } else {
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            logi("Requesting permission")
            startLocationPermissionRequest()
        }
    }

    /**
     * Shows a [Snackbar].
     *
     * @param snackStrId The id for the string resource for the Snackbar text.
     * @param actionStrId The text of the action item.
     * @param listener The listener associated with the Snackbar action.
     */
    private fun showSnackbar(
            snackStrId: Int,
            actionStrId: Int = 0,
            listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
                findViewById(android.R.id.content), getString(snackStrId),
                LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        logi("onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                grantResults.isEmpty() -> logi("User interaction was cancelled.")

                // Permission granted.
                (grantResults[0] == PERMISSION_GRANTED) -> requestUpdate()

                else -> {
                    showSnackbar(R.string.permission_denied_explanation, R.string.settings) {
                        // Build intent that displays the App settings screen.
                        val intent = Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.LIBRARY_PACKAGE_NAME, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    /**
     * Removes activity recognition updates using
     * [ActivityRecognitionClient.removeActivityUpdates]. Registers success and
     * failure callbacks.
     */
    fun removeActivityUpdatesButtonHandler(view: View?) {
        val task = mActivityRecognitionClient.removeActivityUpdates(
                activityDetectionPendingIntent
        )
        task.addOnSuccessListener {
            Toast.makeText(
                    this,
                    getString(R.string.activity_updates_removed),
                    Toast.LENGTH_SHORT
            )
                    .show()
            updatesRequestedState = false
            // Reset the display.
            mAdapter.updateActivities(ArrayList())
        }
        task.addOnFailureListener {
            logw("Failed to enable activity recognition.")
            Toast.makeText(
                    this, getString(R.string.activity_updates_not_removed),
                    Toast.LENGTH_SHORT
            ).show()
            updatesRequestedState = true
        }
    }// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
// requestActivityUpdates() and removeActivityUpdates().

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private val activityDetectionPendingIntent: PendingIntent
        get() {
            val intent = Intent(this, DetectedActivitiesIntentService::class.java)
            // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
// requestActivityUpdates() and removeActivityUpdates().
            return PendingIntent.getService(this, 0, intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }

    /**
     * Ensures that only one button is enabled at any time. The Request Activity Updates button is
     * enabled if the user hasn't yet requested activity updates. The Remove Activity Updates button
     * is enabled if the user has requested activity updates.
     */
    private fun setButtonsEnabledState() {
        if (updatesRequestedState) {
            binding.requestActivityUpdatesButton.isEnabled = false
            binding.removeActivityUpdatesButton.isEnabled = true
        } else {
            binding.requestActivityUpdatesButton.isEnabled = true
            binding.removeActivityUpdatesButton.isEnabled = false
        }
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private var updatesRequestedState: Boolean
        get() = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, false)
        private set(requesting) {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, requesting)
                    .apply()
            setButtonsEnabledState()
        }

    /**
     * Processes the list of freshly detected activities. Asks the adapter to update its list of
     * DetectedActivities with new `DetectedActivity` objects reflecting the latest detected
     * activities.
     */
    private fun updateDetectedActivitiesList() {
        val detectedActivities = Utils.detectedActivitiesFromJson(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(Constants.KEY_DETECTED_ACTIVITIES, "")
        )
        mAdapter.updateActivities(detectedActivities)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, s: String) {
        if (s == Constants.KEY_DETECTED_ACTIVITIES) {
            updateDetectedActivitiesList()
        }
    }

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }
}