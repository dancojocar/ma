package com.example.android.repeatingalarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.android.common.logd
import com.example.android.common.toast
import com.example.android.repeatingalarm.ui.MainScreen

/**
 * A simple launcher activity containing a summary sample description
 * and a few action bar buttons.
 */
class MainActivity : ComponentActivity() {

  private val alarmManager by lazy { getSystemService(ALARM_SERVICE) as AlarmManager }
  private val pendingIntent by lazy {
    Intent(this, MyBroadcastReceiver::class.java).let { intent ->
      PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
    }
  }

  private val requestNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (!isGranted) {
        toast(this, "Please enable notification permission so alarms can alert you.")
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    requestNotificationPermissionIfNeeded()

    setContent {
      MainScreen(
        onOneTimeAlarmClick = ::setOneTimeAlarm,
        onRepeatingAlarmClick = ::setRepeatingAlarm,
        onCancelAlarmClick = ::cancelAlarm,
        onOpenSettingsClick = ::openAppSettings
      )
    }
    logd("Started")
  }

  private fun requestNotificationPermissionIfNeeded() {
    val hasPermission = ContextCompat.checkSelfPermission(
      this,
      Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

    if (!hasPermission) {
      requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }

  private fun setOneTimeAlarm() {
    if (!alarmManager.canScheduleExactAlarms()) {
      val intent = Intent(
        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
        "package:$packageName".toUri()
      )
      startActivity(intent)
      toast(this, "Please allow exact alarms so the alarm can ring.")
      return
    }

    alarmManager.setExactAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      SystemClock.elapsedRealtime() + 10 * 1000,
      pendingIntent
    )
    logd("One time alarm set.")
    toast(this, "A one time alarm was set.")
  }

  private fun setRepeatingAlarm() {
    alarmManager.setRepeating(
      AlarmManager.RTC_WAKEUP,
      SystemClock.elapsedRealtime() + 10 * 1000,
      60 * 1000,
      pendingIntent
    )
    logd("Repeating alarm set.")
    toast(this, "A repeating alarm was set.")
  }

  private fun cancelAlarm() {
    alarmManager.cancel(pendingIntent)
    logd("Alarm canceled.")
    toast(this, "The alarm was canceled.")
  }

  private fun openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = "package:$packageName".toUri()
    }
    startActivity(intent)
  }
}
