package com.google.android.gms.location.sample.activityrecognition

import android.content.Context
import android.util.Log
import com.google.android.gms.location.DetectedActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Utility methods used in this sample.
 */
object Utils {
  /**
   * Returns a human readable String corresponding to a detected activity type.
   */
  fun getActivityString(context: Context, detectedActivityType: Int): String {
    val resources = context.resources
    return when (detectedActivityType) {
      DetectedActivity.IN_VEHICLE -> resources.getString(R.string.in_vehicle)
      DetectedActivity.ON_BICYCLE -> resources.getString(R.string.on_bicycle)
      DetectedActivity.ON_FOOT -> resources.getString(R.string.on_foot)
      DetectedActivity.RUNNING -> resources.getString(R.string.running)
      DetectedActivity.STILL -> resources.getString(R.string.still)
      DetectedActivity.TILTING -> resources.getString(R.string.tilting)
      DetectedActivity.UNKNOWN -> resources.getString(R.string.unknown)
      DetectedActivity.WALKING -> resources.getString(R.string.walking)
      else -> resources.getString(R.string.unidentifiable_activity, detectedActivityType)
    }
  }

  fun detectedActivitiesToJson(detectedActivitiesList: ArrayList<DetectedActivity>?): String {
    val type = object : TypeToken<ArrayList<DetectedActivity?>?>() {}.type
    return Gson().toJson(detectedActivitiesList, type)
  }

  fun detectedActivitiesFromJson(jsonArray: String?): ArrayList<DetectedActivity?> {
    val listType = object : TypeToken<ArrayList<DetectedActivity?>?>() {}.type
    var detectedActivities = Gson().fromJson<ArrayList<DetectedActivity?>>(jsonArray, listType)
    if (detectedActivities == null) {
      detectedActivities = ArrayList()
    }
    return detectedActivities
  }
}

fun Any.logi(messages: String = "no message") {
  Log.i("Location", messages)
}

fun Any.logd(messages: String = "no message", cause: Throwable? = null) {
  Log.d("Location", messages, cause)
}

fun Any.logw(messages: String = "no message", cause: Throwable? = null) {
  Log.w("Location", messages, cause)
}

fun Any.loge(messages: String = "no message", cause: Throwable? = null) {
  Log.e("Location", messages, cause)
}