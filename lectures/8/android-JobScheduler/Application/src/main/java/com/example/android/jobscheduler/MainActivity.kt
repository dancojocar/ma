/*
 * Copyright 2013 The Android Open Source Project
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
package com.example.android.jobscheduler

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.example.android.jobscheduler.service.MyJobService
import kotlinx.android.synthetic.main.sample_main.*
import java.lang.ref.WeakReference

/**
 * Schedules and configures jobs to be executed by a [JobScheduler].
 *
 *
 * [MyJobService] can send messages to this via a [Messenger]
 * that is sent in the Intent that starts the Service.
 */
class MainActivity : Activity() {
  private var mDelayEditText: EditText? = null
  private var mDeadlineEditText: EditText? = null
  private var mDurationTimeEditText: EditText? = null
  private var mWiFiConnectivityRadioButton: RadioButton? = null
  private var mAnyConnectivityRadioButton: RadioButton? = null
  private var mRequiresChargingCheckBox: CheckBox? = null
  private var mRequiresIdleCheckbox: CheckBox? = null
  private var mServiceComponent: ComponentName? = null
  private var mJobId = 0
  // Handler for incoming messages from the service.
  private var mHandler: IncomingMessageHandler? = null

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.sample_main)
    // Set up UI.
    mDelayEditText = findViewById(R.id.delay_time)
    mDurationTimeEditText = findViewById(R.id.duration_time)
    mDeadlineEditText = findViewById(R.id.deadline_time)
    mWiFiConnectivityRadioButton = findViewById(R.id.checkbox_unmetered)
    mAnyConnectivityRadioButton = findViewById(R.id.checkbox_any)
    mRequiresChargingCheckBox = findViewById(R.id.checkbox_charging)
    mRequiresIdleCheckbox = findViewById(R.id.checkbox_idle)
    mServiceComponent = ComponentName(this, MyJobService::class.java)
    mHandler = IncomingMessageHandler(this)
    schedule_button.setOnClickListener { scheduleJob() }
    cancel_button.setOnClickListener { cancelAllJobs() }
    finished_button.setOnClickListener { finishJob() }
  }

  override fun onStop() { // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
// and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
// to stopService() won't prevent scheduled jobs to be processed. However, failing
// to call stopService() would keep it alive indefinitely.
    stopService(Intent(this, MyJobService::class.java))
    super.onStop()
  }

  override fun onStart() {
    super.onStart()
    // Start service and provide it a way to communicate with this class.
    val startServiceIntent = Intent(this, MyJobService::class.java)
    val messengerIncoming = Messenger(mHandler)
    startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming)
    startService(startServiceIntent)
  }

  /**
   * Executed when user clicks on SCHEDULE JOB.
   */
  private fun scheduleJob() {
    val builder = JobInfo.Builder(mJobId++, mServiceComponent!!)
    val delay = mDelayEditText!!.text.toString()
    if (!TextUtils.isEmpty(delay)) {
      builder.setMinimumLatency(java.lang.Long.valueOf(delay) * 1000)
    }
    val deadline = mDeadlineEditText!!.text.toString()
    if (!TextUtils.isEmpty(deadline)) {
      builder.setOverrideDeadline(java.lang.Long.valueOf(deadline) * 1000)
    }
    val requiresUnmetered = mWiFiConnectivityRadioButton!!.isChecked
    val requiresAnyConnectivity = mAnyConnectivityRadioButton!!.isChecked
    if (requiresUnmetered) {
      builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
    } else if (requiresAnyConnectivity) {
      builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
    }
    builder.setRequiresDeviceIdle(mRequiresIdleCheckbox!!.isChecked)
    builder.setRequiresCharging(mRequiresChargingCheckBox!!.isChecked)
    // Extras, work duration.
    val extras = PersistableBundle()
    var workDuration = mDurationTimeEditText!!.text.toString()
    if (TextUtils.isEmpty(workDuration)) {
      workDuration = "1"
    }
    extras.putLong(WORK_DURATION_KEY, java.lang.Long.valueOf(workDuration) * 1000)
    builder.setExtras(extras)
    // Schedule job
    logd("Scheduling job")
    val tm = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    tm.schedule(builder.build())
  }

  /**
   * Executed when user clicks on CANCEL ALL.
   */
  private fun cancelAllJobs() {
    val tm = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    tm.cancelAll()
    Toast.makeText(this@MainActivity, R.string.all_jobs_cancelled, Toast.LENGTH_SHORT).show()
  }

  /**
   * Executed when user clicks on FINISH LAST TASK.
   */
  private fun finishJob() {
    val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    val allPendingJobs = jobScheduler.allPendingJobs
    if (allPendingJobs.size > 0) { // Finish the last one
      val jobId = allPendingJobs[0].id
      jobScheduler.cancel(jobId)
      Toast.makeText(
          this@MainActivity, String.format(getString(R.string.cancelled_job), jobId),
          Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(
          this@MainActivity, getString(R.string.no_jobs_to_cancel),
          Toast.LENGTH_SHORT).show()
    }
  }

  /**
   * A [Handler] allows you to send messages associated with a thread. A [Messenger]
   * uses this handler to communicate from [MyJobService]. It's also used to make
   * the start and stop views blink for a short period.
   */
  private class IncomingMessageHandler internal constructor(activity: MainActivity) : Handler() {
    // Prevent possible leaks with a weak reference.
    private val mActivity: WeakReference<MainActivity> = WeakReference(activity)

    override fun handleMessage(msg: Message) {
      val mainActivity = mActivity.get()
          ?: // Activity is no longer available, exit.
          return
      val showStartView = mainActivity.findViewById<View>(R.id.onstart_textview)
      val showStopView = mainActivity.findViewById<View>(R.id.onstop_textview)
      val m: Message
      when (msg.what) {
        MSG_COLOR_START -> {
          // Start received, turn on the indicator and show text.
          showStartView.setBackgroundColor(getColor(mainActivity, R.color.start_received))
          updateParamsTextView(msg.obj, "started")
          // Send a message to turn it off after a second.
          m = Message.obtain(this, MSG_NO_COLOR_START)
          sendMessageDelayed(m, 1000L)
        }
        MSG_COLOR_STOP -> {
          // Stop received, turn on the indicator and show text.
          showStopView.setBackgroundColor(getColor(mainActivity, R.color.stop_received))
          updateParamsTextView(msg.obj, "stopped")
          // Send a message to turn it off after a second.
          m = obtainMessage(MSG_NO_COLOR_STOP)
          sendMessageDelayed(m, 2000L)
        }
        MSG_NO_COLOR_START -> {
          showStartView.setBackgroundColor(getColor(mainActivity, R.color.none_received))
          updateParamsTextView(null, "")
        }
        MSG_NO_COLOR_STOP -> {
          showStopView.setBackgroundColor(getColor(mainActivity, R.color.none_received))
          updateParamsTextView(null, "")
        }
      }
    }

    private fun updateParamsTextView(jobId: Any?, action: String) {
      val paramsTextView = mActivity.get()!!.findViewById<TextView>(R.id.task_params)
      if (jobId == null) {
        paramsTextView.text = ""
        return
      }
      val jobIdText = jobId.toString()
      paramsTextView.text = String.format("Job ID %s %s", jobIdText, action)
    }

    private fun getColor(context: Context, @ColorRes color: Int): Int {
      return ContextCompat.getColor(context, color)
    }

  }

  companion object {
    const val MSG_NO_COLOR_START = 0
    const val MSG_NO_COLOR_STOP = 1
    const val MSG_COLOR_START = 2
    const val MSG_COLOR_STOP = 3
    const val MESSENGER_INTENT_KEY = BuildConfig.APPLICATION_ID + ".MESSENGER_INTENT_KEY"
    const val WORK_DURATION_KEY = BuildConfig.APPLICATION_ID + ".WORK_DURATION_KEY"
  }
}