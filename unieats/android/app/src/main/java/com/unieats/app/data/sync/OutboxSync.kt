package com.unieats.app.data.sync

import android.util.Log
import androidx.room.withTransaction
import com.unieats.app.data.auth.AuthTokens
import com.unieats.app.data.local.OutboxEntity
import com.unieats.app.data.local.OutboxType
import com.unieats.app.data.local.UniEatsDatabase
import com.unieats.app.data.local.toDomain
import com.unieats.app.data.local.toEntity
import com.unieats.app.data.model.ConflictResponse
import com.unieats.app.data.model.Spot
import com.unieats.app.data.model.SpotPatch
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
        if (tokens.token() == null) return ReplayResult.NeedsLogin
        for (seq in outboxDao.getAllSeqs()) {
            val op = claim(seq) ?: continue
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

    /**
     * Explicit sign out: nothing queued may be sent under the next user's token. The optimistic
     * rows go too; the next load fetches the server copies.
     */
    suspend fun discardAll() = db.withTransaction {
        outboxDao.deleteAll()
        spotDao.deletePending()
    }

    /**
     * Sign in as [userId]: edits another user left queued (their session ended with a 401 or a
     * biometric change) are dropped; this user's own edits stay for replay.
     */
    suspend fun discardOtherUsers(userId: String) = db.withTransaction {
        outboxDao.deleteNotOwnedBy(userId)
        spotDao.deletePendingWithoutOps()
    }

    /** Reads the op fresh and marks it attempted, so editSpot stops merging into it from now on. */
    private suspend fun claim(seq: Long): OutboxEntity? = db.withTransaction {
        outboxDao.getBySeq(seq)?.also { outboxDao.markAttempted(seq) }
    }

    private suspend fun replayUpdate(op: OutboxEntity, patch: SpotPatch) {
        try {
            val saved = api.patchSpot(op.entityId, patch, idempotencyKey = op.opId)
            finish(op, saved, acceptedOurs = true)
        } catch (e: ClientRequestException) {
            when (e.response.status) {
                HttpStatusCode.Conflict -> resolveConflict(op, patch, e.response.body<ConflictResponse>().spot)
                HttpStatusCode.Unauthorized -> throw e
                HttpStatusCode.NotFound -> db.withTransaction {
                    spotDao.deleteById(op.entityId)
                    outboxDao.deleteByEntityId(op.entityId)
                }
                else -> {
                    Log.w(TAG, "server rejected ${op.opId} (${e.response.status}), dropping it")
                    db.withTransaction {
                        if (outboxDao.deleteByOpId(op.opId) > 0 && outboxDao.countFor(op.entityId) == 0) {
                            spotDao.markSynced(op.entityId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun resolveConflict(op: OutboxEntity, patch: SpotPatch, server: Spot) {
        val local = spotDao.getById(op.entityId)?.toDomain()?.copy(updatedAt = patch.updatedAt)
        if (local == null || SyncConflictResolver.resolve(local, server) == server) {
            finish(op, server, acceptedOurs = false)
        } else {
            // Client wins: re-send on top of the server's version under a fresh key.
            val forced = api.patchSpot(op.entityId, patch.copy(updatedAt = server.updatedAt), lwwKey(op.opId))
            finish(op, forced, acceptedOurs = true)
        }
    }

    private fun lwwKey(opId: String) = UUID.nameUUIDFromBytes("$opId-lww".toByteArray()).toString()

    /**
     * The user may have edited the spot again while this request was in flight. That edit is a
     * newer op for the same spot, so the server copy must not overwrite the row while one exists.
     * If the server took our change, the newer edit was made on top of it: move its base version
     * to the server's new updatedAt, or it would get a 409 against our own write.
     */
    private suspend fun finish(op: OutboxEntity, serverSpot: Spot, acceptedOurs: Boolean) = db.withTransaction {
        if (outboxDao.deleteByOpId(op.opId) == 0) return@withTransaction
        if (outboxDao.countFor(op.entityId) == 0) {
            spotDao.upsert(serverSpot.toEntity(pendingSync = false))
        } else if (acceptedOurs) {
            outboxDao.findUnattempted(op.entityId, OutboxType.UPDATE)?.let { newer ->
                rebase(newer, serverSpot.updatedAt)
            }
        }
    }

    private suspend fun rebase(newer: OutboxEntity, serverVersion: Long) {
        val patch = json.decodeFromString<SpotPatch>(newer.payload)
        outboxDao.update(newer.copy(payload = json.encodeToString(patch.copy(updatedAt = serverVersion))))
        spotDao.setBaseVersion(newer.entityId, serverVersion)
    }
}
