package com.unieats.app.data.repository

import androidx.room.withTransaction
import com.unieats.app.data.local.OutboxEntity
import com.unieats.app.data.local.OutboxType
import com.unieats.app.data.local.UniEatsDatabase
import com.unieats.app.data.local.toDomain
import com.unieats.app.data.local.toEntity
import com.unieats.shared.Category
import com.unieats.shared.NewReview
import com.unieats.shared.Review
import com.unieats.shared.Spot
import com.unieats.shared.SpotPatch
import com.unieats.app.data.remote.ApiClient
import com.unieats.app.data.remote.LiveEvent
import com.unieats.app.data.remote.LiveUpdates
import com.unieats.app.data.remote.apiCall
import com.unieats.app.data.sync.SyncScheduler
import com.unieats.app.di.ApplicationScope
import com.unieats.app.notification.SpotNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room is the single source of truth: screens observe the database, network results and live
 * events are written into it, and user edits go to the database first and the outbox second.
 */
@Singleton
class DefaultEatsRepository @Inject constructor(
    private val db: UniEatsDatabase,
    private val api: ApiClient,
    private val json: Json,
    private val syncScheduler: SyncScheduler,
    private val notifications: SpotNotificationService,
    liveUpdates: LiveUpdates,
    @ApplicationScope appScope: CoroutineScope
) : EatsRepository {
    private val spotDao = db.spotDao()
    private val reviewDao = db.reviewDao()
    private val outboxDao = db.outboxDao()

    // Shared by every screen that collects it and closed as soon as none does (each ViewModel
    // adds its own 5 s grace period via stateIn).
    override val liveConnection: Flow<Boolean> = liveUpdates.events()
        .onEach(::applyLiveEvent)
        .map { it != LiveEvent.Disconnected }
        .onStart { emit(false) }
        .distinctUntilChanged()
        .shareIn(appScope, SharingStarted.WhileSubscribed(replayExpirationMillis = 0), replay = 1)

    override fun observeSpots(query: String, category: Category?): Flow<List<Spot>> =
        spotDao.observeFiltered(query.trim(), category?.apiValue).map { rows -> rows.map { it.toDomain() } }

    override fun observeSpot(id: String): Flow<Spot?> = spotDao.observeById(id).map { it?.toDomain() }

    override fun observeReviews(spotId: String): Flow<List<Review>> =
        reviewDao.observeBySpotId(spotId).map { rows -> rows.map { it.toDomain() } }

    override fun observePendingSpotIds(): Flow<Set<String>> = spotDao.observePendingIds().map { it.toSet() }

    override fun observePendingChanges(): Flow<Int> = outboxDao.observeCount()

    override suspend fun refreshSpots(page: Int, query: String, category: Category?): Result<Boolean> = apiCall {
        val response = api.listSpots(page, query, category)
        saveFromServer(response.spots)
        response.hasNextPage
    }

    override suspend fun refreshAllSpots(): Result<Unit> = apiCall {
        var page = 1
        do {
            val response = api.listSpots(page++, query = "", category = null)
            saveFromServer(response.spots)
        } while (response.hasNextPage)
    }

    override suspend fun refreshSpot(id: String): Result<Unit> = apiCall { saveFromServer(listOf(api.getSpot(id))) }

    override suspend fun refreshReviews(spotId: String): Result<Unit> = apiCall {
        reviewDao.upsertAll(api.getReviews(spotId).map { it.toEntity() })
    }

    override suspend fun addReview(spotId: String, stars: Int, text: String): Result<Unit> = apiCall {
        val saved = api.addReview(spotId, NewReview(stars, text.trim()))
        reviewDao.upsertAll(listOf(saved.toEntity()))
    }

    override suspend fun editSpot(id: String, name: String, description: String, openNow: Boolean) {
        db.withTransaction {
            val current = spotDao.getById(id) ?: return@withTransaction
            spotDao.upsert(
                current.copy(name = name, description = description, openNow = openNow, pendingSync = true)
            )
            // The row keeps the server updatedAt it was edited from; that is the version the
            // server compares against on replay.
            val payload = json.encodeToString(SpotPatch(name, description, openNow, current.updatedAt))
            val queued = outboxDao.findPending(id, OutboxType.UPDATE)
            if (queued != null) {
                outboxDao.update(queued.copy(opId = UUID.randomUUID().toString(), payload = payload))
            } else {
                outboxDao.insert(
                    OutboxEntity(
                        opId = UUID.randomUUID().toString(),
                        type = OutboxType.UPDATE,
                        entityId = id,
                        payload = payload,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
        syncScheduler.requestSync()
    }

    /** Upsert by id, never delete-then-insert, and never overwrite a row with unsynced edits. */
    private suspend fun saveFromServer(spots: List<Spot>) = db.withTransaction {
        val pendingIds = spotDao.getPendingSync().map { it.id }.toSet()
        spotDao.upsertAll(spots.filterNot { it.id in pendingIds }.map { it.toEntity() })
    }

    private suspend fun applyLiveEvent(event: LiveEvent) {
        when (event) {
            is LiveEvent.SpotCreated -> saveFromServer(listOf(event.spot))
            is LiveEvent.SpotUpdated -> {
                saveFromServer(listOf(event.spot))
                notifications.notifySpotUpdated(event.spot)
            }
            is LiveEvent.SpotDeleted -> db.withTransaction {
                if (spotDao.getById(event.id)?.pendingSync != true) spotDao.deleteById(event.id)
            }
            LiveEvent.Connected, LiveEvent.Disconnected -> Unit
        }
    }
}
