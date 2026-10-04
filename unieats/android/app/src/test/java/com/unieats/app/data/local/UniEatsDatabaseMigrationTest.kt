package com.unieats.app.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Runs AutoMigration(1 -> 2) against the exported schemas in app/schemas. */
@RunWith(RobolectricTestRunner::class)
class UniEatsDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), UniEatsDatabase::class.java)

    @Test
    fun `version 1 rows survive the migration and queued ops get no owner`() = runTest {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO spots (id, name, category, rating, priceLevel, lat, lng, openNow, photoUrl, description, updatedAt, pendingSync)
                VALUES ('spot-3', 'Pizza Stop (new oven)', 'fastfood', 4.1, 2, 44.426, 26.1015, 1, '', 'Slices', 1000, 1)
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO outbox (opId, type, entityId, payload, createdAt, attempted)
                VALUES ('op-1', 'update', 'spot-3', '{}', 1000, 0)
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true).close()

        val database = Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            UniEatsDatabase::class.java,
            DB_NAME
        ).allowMainThreadQueries().build()
        try {
            val op = database.outboxDao().getBySeq(database.outboxDao().getAllSeqs().single())!!
            assertEquals("op-1", op.opId)
            assertNull(op.userId)
            val spot = database.spotDao().getById("spot-3")!!
            assertEquals("Pizza Stop (new oven)", spot.name)
            assertTrue(spot.pendingSync)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
