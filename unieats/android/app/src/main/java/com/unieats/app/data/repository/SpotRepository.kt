package com.unieats.app.data.repository

import com.unieats.app.data.model.Review
import com.unieats.app.data.model.Spot
import com.unieats.app.data.model.seedReviews
import com.unieats.app.data.model.seedSpots
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotRepository @Inject constructor() {

    fun getSpots(): List<Spot> = seedSpots

    fun getSpot(id: String): Spot? = seedSpots.firstOrNull { it.id == id }

    fun getReviews(spotId: String): List<Review> = seedReviews.filter { it.spotId == spotId }
}
