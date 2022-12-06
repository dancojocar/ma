package com.example.android.repeatingalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService


class MyBroadcastReceiver: BroadcastReceiver() {
  private val CHANNEL_ID = "CHANNELID"
  var mp: MediaPlayer? = null

  override fun onReceive(context: Context?, intent: Intent?) {
    createNotificationChannel(context)
    mp = MediaPlayer.create(context, R.raw.definite)
    mp?.start()

    Toast.makeText(context, "Alarm....", Toast.LENGTH_LONG).show()

    val builder: NotificationCompat.Builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher)
      .setContentTitle("Sample Not. Title")
      .setContentText("Sample Notification Text")
      .setStyle(NotificationCompat.BigTextStyle())

    val notificationManager = NotificationManagerCompat.from(context)

    // notificationId is a unique int for each notification that you must define

    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(12345, builder.build())
  }

  fun createNotificationChannel(context: Context?) {
    val name: CharSequence = "channel Name"
    val description = "channel Description"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, name, importance)
    channel.description = description
    // Register the channel with the system; you can't change the importance
    // or other notification behaviors after this
    // Register the channel with the system; you can't change the importance
    // or other notification behaviors after this
    val notificationManager = getSystemService(
      context!!,
      NotificationManager::class.java
    )
    notificationManager!!.createNotificationChannel(channel)
  }
}