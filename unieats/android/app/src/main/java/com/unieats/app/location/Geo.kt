package com.unieats.app.location

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

const val NEARBY_RADIUS_METERS = 2_000.0

private const val EARTH_RADIUS_METERS = 6_371_000.0

/** Great-circle (haversine) distance in metres. */
fun distanceMeters(from: LatLng, to: LatLng): Double {
    val dLat = Math.toRadians(to.lat - from.lat)
    val dLng = Math.toRadians(to.lng - from.lng)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(from.lat)) * cos(Math.toRadians(to.lat)) * sin(dLng / 2) * sin(dLng / 2)
    return EARTH_RADIUS_METERS * 2 * atan2(sqrt(a), sqrt(1 - a))
}
