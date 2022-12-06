package com.example.waterwidget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.Row
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.waterwidget.WaterWidget.Companion.RECOMMENDED_DAILY_GLASSES
import com.example.waterwidget.WaterWidget.Companion.WATER_WIDGET_PREFS_KEY

@Composable
fun WaterWidgetContent(
    modifier: GlanceModifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        val prefs = currentState<Preferences>()
        val glassesOfWater = prefs[intPreferencesKey(WATER_WIDGET_PREFS_KEY)] ?: 0
        WaterWidgetCounter(
            context = context,
            glassesOfWater = glassesOfWater,
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
        )
        WaterWidgetGoal(
            context = context,
            glassesOfWater = glassesOfWater,
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
        )
        WaterWidgetButtonLayout(
            modifier = GlanceModifier
                .fillMaxSize()
                .defaultWeight()
        )
    }
}

@Composable
fun WaterWidgetCounter(
    context: Context,
    glassesOfWater: Int,
    modifier: GlanceModifier
) {
    Text(
        text = context.getString(
            R.string.glasses_of_water_format,
            glassesOfWater
        ),
        modifier = modifier,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = ColorProvider(
                color = Color.White
            )
        ),
    )
}

@Composable
fun WaterWidgetGoal(
    context: Context,
    glassesOfWater: Int,
    modifier: GlanceModifier
) {
    Text(
        text =
        when {
            glassesOfWater >= RECOMMENDED_DAILY_GLASSES -> context.getString(
                R.string.goal_met
            )
            else -> context.getString(
                R.string.water_goal,
                RECOMMENDED_DAILY_GLASSES
            )
        },
        modifier = modifier,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = ColorProvider(
                color = Color.White
            )
        ),
    )
}

@Composable
fun WaterWidgetButtonLayout(
    modifier: GlanceModifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(
                resId = R.drawable.ic_baseline_delete_outline_24
            ),
            contentDescription = null,
            modifier = GlanceModifier
                .clickable(
                    onClick = actionRunCallback<ClearWaterClickAction>()
                )
                .defaultWeight()
        )
        Image(
            provider = ImageProvider(
                resId = R.drawable.ic_baseline_add_24
            ),
            contentDescription = null,
            modifier = GlanceModifier
                .clickable(
                    onClick = actionRunCallback<AddWaterClickAction>()
                )
                .defaultWeight()
        )
    }
}