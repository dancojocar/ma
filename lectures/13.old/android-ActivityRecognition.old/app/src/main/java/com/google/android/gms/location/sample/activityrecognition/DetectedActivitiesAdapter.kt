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

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.location.DetectedActivity
import java.util.*

/**
 * Adapter that is backed by an array of `DetectedActivity` objects. Finds UI elements in the
 * detected_activity layout and populates each element with data from a DetectedActivity
 * object.
 */
internal class DetectedActivitiesAdapter(
  context: Context?,
  detectedActivities: ArrayList<DetectedActivity?>?
) : ArrayAdapter<DetectedActivity?>(context!!, 0, detectedActivities!!) {
  override fun getView(position: Int, view: View?, parent: ViewGroup): View {
    var viewCopy = view
    val detectedActivity = getItem(position)
    if (viewCopy == null) {
      viewCopy = LayoutInflater.from(context).inflate(
        R.layout.detected_activity, parent, false
      )
    }
    // Find the UI widgets.
    val activityName = viewCopy!!.findViewById<View>(R.id.detected_activity_name) as TextView
    val activityConfidenceLevel = viewCopy.findViewById<View>(
      R.id.detected_activity_confidence_level
    ) as TextView
    val progressBar = viewCopy.findViewById<View>(
      R.id.detected_activity_progress_bar
    ) as ProgressBar
    // Populate widgets with values.
    if (detectedActivity != null) {
      activityName.text = Utils.getActivityString(
        context,
        detectedActivity.type
      )
      activityConfidenceLevel.text = context.getString(
        R.string.percent,
        detectedActivity.confidence
      )
      progressBar.progress = detectedActivity.confidence
    }
    return viewCopy
  }

  /**
   * Process list of recently detected activities and updates the list of `DetectedActivity`
   * objects backing this adapter.
   *
   * @param detectedActivities the freshly detected activities
   */
  fun updateActivities(detectedActivities: ArrayList<DetectedActivity?>?) {
    val detectedActivitiesMap = HashMap<Int, Int?>()
    for (activity in detectedActivities!!) {
      detectedActivitiesMap[activity!!.type] = activity.confidence
    }
    // Every time we detect new activities, we want to reset the confidence level of ALL
// activities that we monitor. Since we cannot directly change the confidence
// of a DetectedActivity, we use a temporary list of DetectedActivity objects. If an
// activity was freshly detected, we use its confidence level. Otherwise, we set the
// confidence level to zero.
    val tempList = ArrayList<DetectedActivity>()
    for (i in Constants.MONITORED_ACTIVITIES.indices) {
      val confidence =
        if (detectedActivitiesMap.containsKey(Constants.MONITORED_ACTIVITIES[i])) detectedActivitiesMap[Constants.MONITORED_ACTIVITIES[i]]!! else 0
      tempList.add(
        DetectedActivity(
          Constants.MONITORED_ACTIVITIES[i],
          confidence
        )
      )
    }
    // Remove all items.
    this.clear()
    // Adding the new list items notifies attached observers that the underlying data has
// changed and views reflecting the data should refresh.
    for (detectedActivity in tempList) {
      add(detectedActivity)
    }
  }
}