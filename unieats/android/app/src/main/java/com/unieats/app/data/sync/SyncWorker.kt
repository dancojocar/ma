package com.unieats.app.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val outboxSync: OutboxSync
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = when (outboxSync.replay()) {
        ReplayResult.Done -> Result.success()
        ReplayResult.RetryLater -> Result.retry()
        // Signing in calls SyncScheduler.requestSync() again.
        ReplayResult.NeedsLogin -> Result.failure()
    }

    companion object {
        const val NAME = "outbox-sync"
    }
}
