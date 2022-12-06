package com.example.waterwidget

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.example.waterwidget.WaterWidget.Companion.MAX_GLASSES
import com.example.waterwidget.WaterWidget.Companion.WATER_WIDGET_PREFS_KEY

class AddWaterClickAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) {
            it.toMutablePreferences()
                .apply {
                    val glassesOfWater = this[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] ?: 0
                    if (glassesOfWater < MAX_GLASSES) {
                        this[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] = glassesOfWater + 1
                    }
                }
        }
        WaterWidget().update(context, glanceId)
    }
}

class ClearWaterClickAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) {
            it.toMutablePreferences()
                .apply {
                    this[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] = 0
                }
        }
        WaterWidget().update(context, glanceId)
    }
}