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
package com.google.android.gms.location.sample.geofencing

import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Constants used in this sample.
 */
internal object Constants {
  private const val PACKAGE_NAME = "com.google.android.gms.location.Geofence"
  const val GEOFENCES_ADDED_KEY = "$PACKAGE_NAME.GEOFENCES_ADDED_KEY"
  /**
   * Used to set an expiration time for a geofence. After this amount of time Location Services
   * stops tracking the geofence.
   */
  private const val GEOFENCE_EXPIRATION_IN_HOURS: Long = 12
  /**
   * For this sample, geofences expire after twelve hours.
   */
  const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
  const val GEOFENCE_RADIUS_IN_METERS = 1000f // 1 km
  /**
   * Map for storing information about airports in the San Francisco bay area.
   */
  val LANDMARKS = HashMap<String, LatLng>()

  init {
    // San Francisco International Airport.
    LANDMARKS["SFO"] = LatLng(37.621313, -122.378955)
    // Googleplex.
    LANDMARKS["GOOGLE"] = LatLng(37.422611, -122.0840577)
    // FSEGA Campus.
    LANDMARKS["FSEGA"] = LatLng(46.7731847, 23.6187553)
  }
}