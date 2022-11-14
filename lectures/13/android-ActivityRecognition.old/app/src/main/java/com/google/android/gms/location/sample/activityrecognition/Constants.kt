/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.location.sample.activityrecognition

import com.google.android.gms.location.DetectedActivity

/**
 * Constants used in this sample.
 */
internal object Constants {
  /**
   * The desired time between activity detections. Larger values result in fewer activity
   * detections while improving battery life. A value of 0 results in activity detections at the
   * fastest possible rate.
   */
  const val DETECTION_INTERVAL_IN_MILLISECONDS = 30 * 1000 // 30 seconds
      .toLong()
  /**
   * List of DetectedActivity types that we monitor in this sample.
   */
  val MONITORED_ACTIVITIES = intArrayOf(
      DetectedActivity.STILL,
      DetectedActivity.ON_FOOT,
      DetectedActivity.WALKING,
      DetectedActivity.RUNNING,
      DetectedActivity.ON_BICYCLE,
      DetectedActivity.IN_VEHICLE,
      DetectedActivity.TILTING,
      DetectedActivity.UNKNOWN
  )
  private const val PACKAGE_NAME = "com.google.android.gms.location.activityrecognition"
  const val KEY_ACTIVITY_UPDATES_REQUESTED = PACKAGE_NAME +
      ".ACTIVITY_UPDATES_REQUESTED"
  const val KEY_DETECTED_ACTIVITIES = "$PACKAGE_NAME.DETECTED_ACTIVITIES"
}