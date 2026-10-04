package com.unieats.app.data.local

import com.unieats.shared.Category
import com.unieats.shared.Review
import com.unieats.shared.Spot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntityMappingTest {

    private val spot = Spot(
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
        updatedAt = 1_700_000_002_000
    )

    @Test
    fun `spot survives a round trip through SpotEntity`() {
        assertEquals(spot, spot.toEntity().toDomain())
    }

    @Test
    fun `pendingSync defaults to false and can be set`() {
        assertFalse(spot.toEntity().pendingSync)
        assertTrue(spot.toEntity(pendingSync = true).pendingSync)
    }

    @Test
    fun `category is stored with its API value so SQL filters match the server`() {
        Category.entries.forEach { category ->
            val entity = spot.copy(category = category).toEntity()
            assertEquals(category.apiValue, entity.category)
            assertEquals(category, entity.toDomain().category)
        }
        assertEquals("fastfood", spot.toEntity().category)
    }

    @Test
    fun `review survives a round trip through ReviewEntity`() {
        val review = Review("review-4", "spot-3", "Andrei", 4, "Crispy crust every time.", 1_700_000_300_000)
        assertEquals(review, review.toEntity().toDomain())
    }
}
