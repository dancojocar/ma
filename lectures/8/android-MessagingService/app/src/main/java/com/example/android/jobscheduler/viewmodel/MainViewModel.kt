package com.example.android.jobscheduler.viewmodel

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.android.jobscheduler.MyJobService
import com.example.android.jobscheduler.R
import com.example.android.jobscheduler.WORK_DURATION_KEY
import java.util.concurrent.TimeUnit

data class JobStatus(val jobId: Int?, val action: String?)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val jobScheduler = application.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
    private val serviceComponent = ComponentName(application, MyJobService::class.java)
    private var jobId = 0

    private val _jobStatus = MutableLiveData<JobStatus?>()
    val jobStatus: LiveData<JobStatus?> = _jobStatus

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    fun clearToast() {
        _toast.value = null
    }

    fun onJobStarted(jobId: Int?) {
        _jobStatus.value = JobStatus(jobId, "started")
    }

    fun onJobStopped(jobId: Int?) {
        _jobStatus.value = JobStatus(jobId, "stopped")
    }

    fun scheduleJob(delay: String, deadline: String, requiresCharging: Boolean, requiresIdle: Boolean, workDuration: String) {
        val builder = JobInfo.Builder(jobId++, serviceComponent)

        if (delay.isNotEmpty()) {
            builder.setMinimumLatency(delay.toLong() * TimeUnit.SECONDS.toMillis(1))
        }

        if (deadline.isNotEmpty()) {
            builder.setOverrideDeadline(deadline.toLong() * TimeUnit.SECONDS.toMillis(1))
        }

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)

        val extras = PersistableBundle()
        val duration = if (workDuration.isEmpty()) "1" else workDuration
        extras.putLong(WORK_DURATION_KEY, duration.toLong() * TimeUnit.SECONDS.toMillis(1))

        builder.run {
            setRequiresDeviceIdle(requiresIdle)
            setRequiresCharging(requiresCharging)
            setExtras(extras)
        }

        jobScheduler.schedule(builder.build())
    }

    fun cancelAllJobs() {
        jobScheduler.cancelAll()
        _toast.value = getApplication<Application>().getString(R.string.all_jobs_cancelled)
    }

    fun finishJob() {
        val allPendingJobs = jobScheduler.allPendingJobs
        if (allPendingJobs.size > 0) {
            val id = allPendingJobs.first().id
            jobScheduler.cancel(id)
            _toast.value = getApplication<Application>().getString(R.string.cancelled_job, id)
        } else {
            _toast.value = getApplication<Application>().getString(R.string.no_jobs_to_cancel)
        }
    }
}
