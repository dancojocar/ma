package com.unieats.app.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.unieats.app.R
import com.unieats.shared.Spot
import com.unieats.app.ui.navigation.DEEP_LINK_APP
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Local notifications driven by the live WebSocket (no FCM/APNs). */
@Singleton
class SpotNotificationService @Inject constructor(@param:ApplicationContext private val context: Context) {

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.notification_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun notifySpotUpdated(spot: Spot) {
        // Below API 33 the permission does not exist and checkSelfPermission reports it denied.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        // Tapping opens the spot through the l04 deep link.
        val open = Intent(Intent.ACTION_VIEW, "$DEEP_LINK_APP/${spot.id}".toUri()).setPackage(context.packageName)
        val pendingIntent = PendingIntent.getActivity(
            context,
            spot.id.hashCode(),
            open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.notification_spot_updated, spot.name))
            .setContentText(spot.description)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(spot.id.hashCode(), notification)
    }

    private companion object {
        const val CHANNEL_ID = "spot_updates"
    }
}
