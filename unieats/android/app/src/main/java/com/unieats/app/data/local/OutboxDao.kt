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

    @Query("SELECT * FROM outbox ORDER BY seq ASC")
    suspend fun getAll(): List<OutboxEntity>

    @Query("SELECT * FROM outbox WHERE entityId = :entityId AND type = :type LIMIT 1")
    suspend fun findPending(entityId: String, type: String): OutboxEntity?

    @Query("SELECT COUNT(*) FROM outbox")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM outbox WHERE opId = :opId")
    suspend fun deleteByOpId(opId: String)
}
