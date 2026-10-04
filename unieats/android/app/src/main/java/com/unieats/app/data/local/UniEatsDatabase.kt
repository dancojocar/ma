package com.unieats.app.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [SpotEntity::class, ReviewEntity::class, OutboxEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class UniEatsDatabase : RoomDatabase() {
    abstract fun spotDao(): SpotDao
    abstract fun reviewDao(): ReviewDao
    abstract fun outboxDao(): OutboxDao
}
