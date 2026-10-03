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

/** One queued write. Rows are replayed in [seq] order; [opId] is sent as the Idempotency-Key. */
@Entity(tableName = "outbox", indices = [Index("opId", unique = true)])
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    val opId: String,
    val type: String,
    val entityId: String,
    val payload: String,
    val createdAt: Long
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
