/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.repeatingalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.MenuItem
import androidx.annotation.RequiresApi
import com.example.android.common.logd
import com.example.android.common.toast


class RepeatingAlarmFragment : Fragment() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setHasOptionsMenu(true)
  }

  @RequiresApi(Build.VERSION_CODES.S)
  override fun onOptionsItemSelected(item: MenuItem): Boolean {

    val pendingIntent = Intent(context, MyBroadcastReceiver::class.java).let { intent ->
      PendingIntent.getBroadcast(context?.applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
    }

    // There are two clock types for alarms, ELAPSED_REALTIME and RTC.
    // ELAPSED_REALTIME uses time since system boot as a reference, and RTC uses UTC (wall
    // clock) time.  This means ELAPSED_REALTIME is suited to setting an alarm according to
    // passage of time (every 15 seconds, 15 minutes, etc), since it isn't affected by
    // timezone/locale.  RTC is better suited for alarms that should be dependant on current
    // locale.

    // Both types have a WAKEUP version, which says to wake up the device if the screen is
    // off.  This is useful for situations such as alarm clocks.  Abuse of this flag is an
    // efficient way to skyrocket the uninstall rate of an application, so use with care.
    // For most situations, ELAPSED_REALTIME will suffice.
    val alarmType = AlarmManager.RTC_WAKEUP

    // The AlarmManager, like most system services, isn't created by application code, but
    // requested from the system.
    val alarmManager = requireActivity().getSystemService(ALARM_SERVICE) as AlarmManager
    when (item.itemId) {
      R.id.oneTime -> {
        alarmManager.setExactAndAllowWhileIdle(
          alarmType,
          SystemClock.elapsedRealtime() + 10 * 1000,
          pendingIntent
        )
        logd("One time alarm set.")
        toast(context, "A one time alarm was set.")
      }
      R.id.repeating -> {
        // setRepeating takes a start delay and period between alarms as arguments.
        // The below code fires after 15 minutes, and repeats every 15 minutes.
        alarmManager.setRepeating(
          alarmType,
          SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
          AlarmManager.INTERVAL_FIFTEEN_MINUTES,
          pendingIntent
        )
        logd("Repeating alarm set.")
        toast(context,"A repeating alarm was set.")
      }
      R.id.cancel_action -> {
        alarmManager.cancel(pendingIntent)
        logd("Alarm canceled.")
        toast(context,"The alarm was canceled.")
      }
    }

    return true
  }

  companion object {
    // This value is defined and consumed by app code, so any value will work.
    // There's no significance to this sample using 0.
    const val REQUEST_CODE = 0
  }
}
