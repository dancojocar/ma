package com.unieats.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(entry: OutboxEntity)

    @Update
    suspend fun update(entry: OutboxEntity)

    @Query("SELECT seq FROM outbox ORDER BY seq ASC")
    suspend fun getAllSeqs(): List<Long>

    @Query("SELECT * FROM outbox WHERE seq = :seq")
    suspend fun getBySeq(seq: Long): OutboxEntity?

    @Query("UPDATE outbox SET attempted = 1 WHERE seq = :seq")
    suspend fun markAttempted(seq: Long)

    @Query("SELECT * FROM outbox WHERE entityId = :entityId AND type = :type AND attempted = 0 LIMIT 1")
    suspend fun findUnattempted(entityId: String, type: String): OutboxEntity?

    @Query("SELECT COUNT(*) FROM outbox WHERE entityId = :entityId")
    suspend fun countFor(entityId: String): Int

    @Query("SELECT COUNT(*) FROM outbox")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM outbox WHERE opId = :opId")
    suspend fun deleteByOpId(opId: String): Int

    @Query("DELETE FROM outbox WHERE entityId = :entityId")
    suspend fun deleteByEntityId(entityId: String)
}
