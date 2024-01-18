/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.platform.ui.appwidgets.rv.list

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import androidx.core.util.SizeFCompat
import androidx.core.widget.createResponsiveSizeAppWidget
import com.example.platform.ui.appwidgets.R

/**
 * Implementation of a list app widget.
 */
class ListAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            ListSharedPrefsUtil.deleteWidgetLayoutIdPref(context, appWidgetId)
        }
    }

    companion object {

        private const val REQUEST_CODE_OPEN_ACTIVITY = 1

        @SuppressLint("RemoteViewLayout")
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
        ) {
            // TODO use deeplink
            val activityIntent =
                context.packageManager.getLaunchIntentForPackage(context.packageName)!!.apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }
            val appOpenIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_OPEN_ACTIVITY,
                activityIntent,
                // API level 31 requires specifying either of
                // PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE
                // See https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            fun constructRemoteViews(
                @LayoutRes widgetLayoutId: Int,
            ) = RemoteViews(context.packageName, widgetLayoutId).apply {
                if (widgetLayoutId == R.layout.widget_grocery_list ||
                    widgetLayoutId == R.layout.widget_grocery_grid
                ) {
                    setTextViewText(
                        R.id.checkbox_list_title,
                        context.resources.getText(R.string.grocery_list)
                    )
                } else if (widgetLayoutId == R.layout.widget_todo_list) {
                    setTextViewText(
                        R.id.checkbox_list_title,
                        context.resources.getText(R.string.todo_list)
                    )
                }
                setOnClickPendingIntent(R.id.checkbox_list_title, appOpenIntent)
            }

            val layoutId = ListSharedPrefsUtil.loadWidgetLayoutIdPref(context, appWidgetId)
            val remoteViews = if (layoutId == R.layout.widget_grocery_list) {
                // Specify the maximum width and height in dp and a layout, which you want to use
                // for the specified size
                val sizes = listOf(SizeFCompat(150f, 150f), SizeFCompat(250f, 150f))
                createResponsiveSizeAppWidget(appWidgetManager, appWidgetId, sizes) { size ->
                    val id = if (size == sizes[0]) {
                        R.layout.widget_grocery_list
                    } else {
                        R.layout.widget_grocery_grid
                    }
                    constructRemoteViews(id)
                }
            } else {
                constructRemoteViews(layoutId)
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
        }
    }
}
