package com.unieats.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {
    @Query(
        """
        SELECT * FROM spots
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
          AND (:category IS NULL OR category = :category)
        ORDER BY name COLLATE NOCASE
        """
    )
    fun observeFiltered(query: String, category: String?): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE id = :id")
    fun observeById(id: String): Flow<SpotEntity?>

    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getById(id: String): SpotEntity?

    @Query("SELECT id FROM spots WHERE pendingSync = 1")
    fun observePendingIds(): Flow<List<String>>

    @Query("SELECT * FROM spots WHERE pendingSync = 1")
    suspend fun getPendingSync(): List<SpotEntity>

    @Upsert
    suspend fun upsert(spot: SpotEntity)

    @Upsert
    suspend fun upsertAll(spots: List<SpotEntity>)

    @Query("UPDATE spots SET pendingSync = 0 WHERE id = :id")
    suspend fun markSynced(id: String)

    @Query("DELETE FROM spots WHERE id = :id")
    suspend fun deleteById(id: String)
}
