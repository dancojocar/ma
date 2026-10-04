package com.unieats.app.testing

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.unieats.app.data.auth.AuthTokens
import com.unieats.app.data.local.OutboxEntity
import com.unieats.app.data.local.SpotEntity
import com.unieats.app.data.local.UniEatsDatabase
import com.unieats.app.data.local.toEntity
import com.unieats.app.data.remote.ApiClient
import com.unieats.app.data.remote.LiveUpdates
import com.unieats.app.data.repository.DefaultEatsRepository
import com.unieats.app.data.sync.OutboxSync
import com.unieats.app.data.sync.SyncScheduler
import com.unieats.app.di.AppModule
import com.unieats.app.di.createHttpClient
import com.unieats.shared.Category
import com.unieats.shared.Spot
import com.unieats.shared.SpotPatch
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emptyFlow

/**
 * The real data layer (Room in memory, OutboxSync, DefaultEatsRepository, ApiClient with the app's
 * HttpClient setup) in front of a [FakeServer]. Needs Robolectric for Room and the Context.
 */
class SyncFixture(scope: CoroutineScope) {

    val json = AppModule.provideJson()
    val db: UniEatsDatabase = Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), UniEatsDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val server = FakeServer(json)

    var userId: String? = USER_1
    val tokens = mockk<AuthTokens>(relaxUnitFun = true) {
        coEvery { token() } answers { if (userId != null) "jwt-$userId" else null }
        coEvery { userId() } answers { userId }
    }
    val syncScheduler = mockk<SyncScheduler>(relaxed = true)

    val api = ApiClient(createHttpClient(server.engine, json, "http://unieats.test/api/", tokens), tokens)
    val outboxSync = OutboxSync(db, api, tokens, json)
    val repository = DefaultEatsRepository(
        db = db,
        api = api,
        json = json,
        tokens = tokens,
        syncScheduler = syncScheduler,
        notifications = mockk(relaxed = true),
        liveUpdates = mockk<LiveUpdates> { every { events() } returns emptyFlow() },
        appScope = scope
    )

    suspend fun seed(vararg spots: Spot) = db.spotDao().upsertAll(spots.map { it.toEntity() })

    suspend fun row(id: String): SpotEntity? = db.spotDao().getById(id)

    suspend fun outbox(): List<OutboxEntity> =
        db.outboxDao().getAllSeqs().mapNotNull { db.outboxDao().getBySeq(it) }

    fun patchOf(op: OutboxEntity): SpotPatch = json.decodeFromString(op.payload)

    fun patchOf(request: FakeServer.Request): SpotPatch = json.decodeFromString(request.body)

    fun close() = db.close()

    companion object {
        const val USER_1 = "user-1"
        const val USER_2 = "user-2"

        val pizzaStop = Spot(
            id = "spot-3",
            name = "Pizza Stop",
            category = Category.FASTFOOD,
            rating = 4.1,
            priceLevel = 2,
            lat = 44.426,
            lng = 26.1015,
            openNow = true,
            photoUrl = "https://picsum.photos/seed/spot-3/400/300",
            description = "Pizza by the slice for students on the go.",
            updatedAt = 1_000
        )

        val espressoLab = pizzaStop.copy(
            id = "spot-2",
            name = "Espresso Lab",
            category = Category.CAFE,
            description = "Third-wave coffee between lectures."
        )
    }
}
