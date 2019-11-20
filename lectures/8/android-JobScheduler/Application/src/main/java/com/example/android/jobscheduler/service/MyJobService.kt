/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.jobscheduler.service

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import com.example.android.jobscheduler.MainActivity
import com.example.android.jobscheduler.logd
import com.example.android.jobscheduler.loge
import com.example.android.jobscheduler.logi

/**
 * Service to handle callbacks from the JobScheduler. Requests scheduled with the JobScheduler
 * ultimately land on this service's "onStartJob" method. It runs jobs for a specific amount of time
 * and finishes them. It keeps the activity updated with changes via a Messenger.
 */
class MyJobService : JobService() {
  private var mActivityMessenger: Messenger? = null
  override fun onCreate() {
    super.onCreate()
    logi("Service created")
  }

  override fun onDestroy() {
    super.onDestroy()
    logi("Service destroyed")
  }

  /**
   * When the app's MainActivity is created, it starts this service. This is so that the
   * activity and this service can communicate back and forth. See "setUiCallback()"
   */
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    mActivityMessenger = intent.getParcelableExtra(MainActivity.MESSENGER_INTENT_KEY)
    return Service.START_NOT_STICKY
  }

  override fun onStartJob(params: JobParameters): Boolean {
    // The work this service "does" is simply to wait for a certain duration and finish
    // the job (on another thread).
    sendMessage(MainActivity.MSG_COLOR_START, params.jobId)
    val duration = params.extras.getLong(MainActivity.WORK_DURATION_KEY)
    // Uses a handler to delay the execution of jobFinished().
    val handler = Handler()
    handler.postDelayed({
      sendMessage(MainActivity.MSG_COLOR_STOP, params.jobId)
      jobFinished(params, false)
    }, duration)
    logi("on start job: ${params.jobId}")
    // Return true as there's more work to be done with this job.
    return true
  }

  override fun onStopJob(params: JobParameters): Boolean {
    // Stop tracking these job parameters, as we've 'finished' executing.
    sendMessage(MainActivity.MSG_COLOR_STOP, params.jobId)
    logi("on stop job: ${params.jobId}")
    // Return false to drop the job.
    return false
  }

  private fun sendMessage(messageID: Int, params: Any?) {
    // If this service is launched by the JobScheduler, there's no callback Messenger. It
    // only exists when the MainActivity calls startService() with the callback in the Intent.
    if (mActivityMessenger == null) {
      logd("Service is bound, not started. There's no callback to send a message to.")
      return
    }
    val m = Message.obtain()
    m.what = messageID
    m.obj = params
    try {
      mActivityMessenger!!.send(m)
    } catch (e: RemoteException) {
      loge("Error passing service object back to activity.", e)
    }
  }
}