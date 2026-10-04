package com.unieats.shared

import kotlin.test.Test
import kotlin.test.assertSame

class SyncConflictResolverTest {

    private fun spot(name: String, updatedAt: Long) = Spot(
        id = "spot-1",
        name = name,
        category = Category.CANTEEN,
        rating = 3.8,
        priceLevel = 1,
        lat = 44.4268,
        lng = 26.1025,
        openNow = true,
        photoUrl = "",
        description = "",
        updatedAt = updatedAt
    )

    @Test
    fun serverWinsWhenItsUpdatedAtIsNewer() {
        val local = spot("local edit", updatedAt = 1_000)
        val server = spot("server edit", updatedAt = 2_000)
        assertSame(server, SyncConflictResolver.resolve(local, server))
    }

    @Test
    fun clientWinsWhenItsUpdatedAtIsNewer() {
        val local = spot("local edit", updatedAt = 3_000)
        val server = spot("server edit", updatedAt = 2_000)
        assertSame(local, SyncConflictResolver.resolve(local, server))
    }

    @Test
    fun clientWinsOnATie() {
        val local = spot("local edit", updatedAt = 2_000)
        val server = spot("server edit", updatedAt = 2_000)
        assertSame(local, SyncConflictResolver.resolve(local, server))
    }
}
