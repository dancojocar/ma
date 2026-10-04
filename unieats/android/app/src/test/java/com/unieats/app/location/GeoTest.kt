package com.unieats.app.location

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoTest {

    private val campus = LatLng(44.427, 26.103)

    @Test
    fun `distance to the same point is zero`() {
        assertEquals(0.0, distanceMeters(campus, campus), 0.001)
    }

    @Test
    fun `one hundredth of a degree of latitude is about 1112 m`() {
        assertEquals(1_112.0, distanceMeters(campus, LatLng(44.437, 26.103)), 2.0)
    }

    @Test
    fun `every canonical spot is inside the 2 km radius of the campus centre`() {
        val pizzaStop = LatLng(44.426, 26.1015)
        val sushiBox = LatLng(44.427, 26.105)
        assertTrue(distanceMeters(campus, pizzaStop) < NEARBY_RADIUS_METERS)
        assertTrue(distanceMeters(campus, sushiBox) < NEARBY_RADIUS_METERS)
    }
}
