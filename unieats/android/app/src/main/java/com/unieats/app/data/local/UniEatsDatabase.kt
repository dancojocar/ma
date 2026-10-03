package com.unieats.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SpotEntity::class, ReviewEntity::class, OutboxEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UniEatsDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun reviewDao(): ReviewDao
    abstract fun outboxDao(): OutboxDao
}
