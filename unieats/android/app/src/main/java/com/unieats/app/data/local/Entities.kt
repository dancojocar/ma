package com.unieats.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.unieats.app.data.model.Category
import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot

@Entity(tableName = "spots")
data class SpotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val priceLevel: Int,
    val lat: Double,
    val lng: Double,
    val openNow: Boolean,
    val photoUrl: String,
    val description: String,
    val updatedAt: Long,
    val pendingSync: Boolean = false
)

@Entity(tableName = "reviews", indices = [Index("spotId")])
data class ReviewEntity(
    @PrimaryKey val id: String,
    val spotId: String,
    val author: String,
    val stars: Int,
    val text: String,
    val createdAt: Long
)

/**
 * One queued write. Rows are replayed in [seq] order; [opId] is sent as the Idempotency-Key.
 * Once [attempted], the request may have reached the server, so the row is never edited again:
 * a retry must resend exactly what the key was first used for. [userId] is the signed-in user
 * who made the edit; another user signing in discards it. Null for ops queued before the column
 * existed (database version 1): those are kept and replayed by whoever signs in.
 */
@Entity(tableName = "outbox", indices = [Index("opId", unique = true), Index("entityId")])
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    val opId: String,
    val type: String,
    val entityId: String,
    val payload: String,
    val createdAt: Long,
    val userId: String?,
    val attempted: Boolean = false
)

object OutboxType {
    const val UPDATE = "update"
}

fun SpotEntity.toDomain(): Spot = Spot(
    id = id,
    name = name,
    category = Category.entries.first { it.apiValue == category },
    rating = rating,
    priceLevel = priceLevel,
    lat = lat,
    lng = lng,
    openNow = openNow,
    photoUrl = photoUrl,
    description = description,
    updatedAt = updatedAt
)

fun Spot.toEntity(pendingSync: Boolean = false): SpotEntity = SpotEntity(
    id = id,
    name = name,
    category = category.apiValue,
    rating = rating,
    priceLevel = priceLevel,
    lat = lat,
    lng = lng,
    openNow = openNow,
    photoUrl = photoUrl,
    description = description,
    updatedAt = updatedAt,
    pendingSync = pendingSync
)

fun ReviewEntity.toDomain(): Review = Review(id, spotId, author, stars, text, createdAt)

fun Review.toEntity(): ReviewEntity = ReviewEntity(id, spotId, author, stars, text, createdAt)
