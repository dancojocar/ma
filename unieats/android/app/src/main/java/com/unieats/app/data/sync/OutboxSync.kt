package com.unieats.app.data.sync

import android.util.Log
import androidx.room.withTransaction
import com.unieats.app.data.auth.AuthTokens
import com.unieats.app.data.local.OutboxEntity
import com.unieats.app.data.local.OutboxType
import com.unieats.app.data.local.UniEatsDatabase
import com.unieats.app.data.local.toDomain
import com.unieats.app.data.local.toEntity
import com.unieats.shared.ConflictResponse
import com.unieats.shared.Spot
import com.unieats.shared.SyncConflictResolver
import com.unieats.shared.SpotPatch
import com.unieats.app.data.remote.ApiClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class ReplayResult { Done, RetryLater, NeedsLogin }

private const val TAG = "OutboxSync"

@Singleton
class OutboxSync @Inject constructor(
    private val db: UniEatsDatabase,
    private val api: ApiClient,
    private val tokens: AuthTokens,
    private val json: Json
) {
    private val spotDao = db.spotDao()
    private val outboxDao = db.outboxDao()

    /** Sends every queued operation in order; stops at the first network/server failure. */
    suspend fun replay(): ReplayResult {
        if (tokens.token == null) return ReplayResult.NeedsLogin
        for (op in outboxDao.getAll()) {
            try {
                when (op.type) {
                    OutboxType.UPDATE -> replayUpdate(op, json.decodeFromString(op.payload))
                    else -> {
                        Log.w(TAG, "dropping unknown outbox type ${op.type}")
                        outboxDao.deleteByOpId(op.opId)
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ClientRequestException) {
                if (e.response.status == HttpStatusCode.Unauthorized) return ReplayResult.NeedsLogin
                Log.w(TAG, "replay of ${op.opId} rejected, will retry", e)
                return ReplayResult.RetryLater
            } catch (e: Exception) {
                Log.w(TAG, "replay of ${op.opId} failed, will retry", e)
                return ReplayResult.RetryLater
            }
        }
        return ReplayResult.Done
    }

    private suspend fun replayUpdate(op: OutboxEntity, patch: SpotPatch) {
        try {
            val saved = api.patchSpot(op.entityId, patch, idempotencyKey = op.opId)
            finish(op, saved)
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Conflict -> resolveConflict(op, patch, e.response.body<ConflictResponse>().spot)
                HttpStatusCode.Unauthorized -> throw e
                HttpStatusCode.NotFound -> db.withTransaction {
                    spotDao.deleteById(op.entityId)
                    outboxDao.deleteByOpId(op.opId)
                }
                else -> {
                    Log.w(TAG, "server rejected ${op.opId} (${e.response.status}), dropping it")
                    db.withTransaction {
                        spotDao.markSynced(op.entityId)
                        outboxDao.deleteByOpId(op.opId)
                    }
                }
            }
        }
    }

    private suspend fun resolveConflict(op: OutboxEntity, patch: SpotPatch, server: Spot) {
        val local = spotDao.getById(op.entityId)?.toDomain()?.copy(updatedAt = patch.updatedAt)
        if (local == null || SyncConflictResolver.resolve(local, server) == server) {
            finish(op, server)
        } else {
            // Client wins: re-send on top of the server's version under a fresh key.
            val forced = api.patchSpot(op.entityId, patch.copy(updatedAt = server.updatedAt), lwwKey(op.opId))
            finish(op, forced)
        }
    }

    private fun lwwKey(opId: String) = UUID.nameUUIDFromBytes("$opId-lww".toByteArray()).toString()

    private suspend fun finish(op: OutboxEntity, serverSpot: Spot) = db.withTransaction {
        spotDao.upsert(serverSpot.toEntity(pendingSync = false))
        outboxDao.deleteByOpId(op.opId)
    }
}
