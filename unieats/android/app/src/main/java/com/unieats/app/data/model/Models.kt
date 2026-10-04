package com.unieats.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Category(val label: String) {
    @SerialName("cafe") CAFE("Cafe"),
    @SerialName("canteen") CANTEEN("Canteen"),
    @SerialName("fastfood") FASTFOOD("Fast food"),
    @SerialName("bakery") BAKERY("Bakery"),
    @SerialName("bar") BAR("Bar");

    val apiValue: String get() = name.lowercase()
}

@Serializable
data class Spot(
    val id: String,
    val name: String,
    val category: Category,
    val rating: Double,
    val priceLevel: Int,
    val lat: Double,
    val lng: Double,
    val openNow: Boolean,
    val photoUrl: String = "",
    val description: String = "",
    val updatedAt: Long
)

@Serializable
data class Review(
    val id: String,
    val spotId: String,
    val author: String,
    val stars: Int,
    val text: String,
    val createdAt: Long
)

@Serializable
data class SpotsPage(
    val spots: List<Spot>,
    val page: Int,
    val hasNextPage: Boolean
)
