package com.unieats.app.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.unieats.app.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val connectivity: ConnectivityObserver,
    @param:ApplicationScope private val appScope: CoroutineScope
) {

    /** Replays the outbox every time the device comes back online. */
    fun start() {
        connectivity.isOnline
            .filter { online -> online }
            .onEach { requestSync() }
            .launchIn(appScope)
    }

    fun requestSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .build()
        // APPEND_OR_REPLACE: an edit made while a replay is running still gets its own run after it.
        WorkManager.getInstance(context)
            .enqueueUniqueWork(SyncWorker.NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
    }
}
