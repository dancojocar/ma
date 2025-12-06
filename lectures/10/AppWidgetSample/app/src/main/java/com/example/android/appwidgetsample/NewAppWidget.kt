package com.example.android.appwidgetsample

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import java.text.DateFormat
import java.util.Date
import androidx.core.content.edit

class NewAppWidget : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NewAppWidgetContent
}

object NewAppWidgetContent : GlanceAppWidget() {
    
    override val stateDefinition = PreferencesGlanceStateDefinition

    val countKey = intPreferencesKey("count")
    val triggerKey = androidx.datastore.preferences.core.longPreferencesKey("trigger")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // We read the local state to ensure we depend on it, 
            // effectively subscribing to updates.
            val prefs = currentState<Preferences>()
            val trigger = prefs[triggerKey]
            
            // Read the actual count from Global SharedPreferences
            // We do this inside the Composable so it re-runs when prefs changes (trigger)
            val globalPrefs = context.getSharedPreferences("com.example.android.appwidgetsample", 0)
            val count = globalPrefs.getInt("count", 0)
            val dateString = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date())
            
            MyContent(count, dateString)
        }
    }

    @Composable
    private fun MyContent(count: Int, dateString: String) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(R.color.white))
                .cornerRadius(16.dp)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Widget Update",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorProvider(R.color.colorPrimary)
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Count: $count",
                style = TextStyle(
                    fontSize = 16.sp,
                    color = ColorProvider(R.color.black)
                )
            )
            Text(
                text = "Last: $dateString",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(R.color.gray)
                )
            )
            Spacer(modifier = GlanceModifier.height(16.dp))
            Button(
                text = "Update Now",
                onClick = actionRunCallback<UpdateAction>()
            )
        }
    }
}

class UpdateAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // 1. Update Global Count
        val globalPrefs = context.getSharedPreferences("com.example.android.appwidgetsample", 0)
        val count = globalPrefs.getInt("count", 0) + 1
        globalPrefs.edit(commit = true) { putInt("count", count) }

        // 2. Force update on ALL widgets by changing their local state (Trigger)
        val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
        val ids = manager.getGlanceIds(NewAppWidgetContent::class.java)
        val now = System.currentTimeMillis()

        ids.forEach { id ->
            try {
                updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
                    prefs.toMutablePreferences().apply {
                        this[NewAppWidgetContent.triggerKey] = now
                    }
                }
                NewAppWidgetContent.update(context, id)
            } catch (e: Exception) {
                // Ignore errors during update to ensure loop continues
            }
        }
    }
}