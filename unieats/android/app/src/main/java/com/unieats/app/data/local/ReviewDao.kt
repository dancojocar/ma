package com.unieats.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE spotId = :spotId ORDER BY createdAt DESC")
    fun observeBySpotId(spotId: String): Flow<List<ReviewEntity>>

    @Upsert
    suspend fun upsertAll(reviews: List<ReviewEntity>)
}
