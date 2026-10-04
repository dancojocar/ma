package com.unieats.app.data.model

enum class Category(val label: String) {
    CAFE("Cafe"),
    CANTEEN("Canteen"),
    FASTFOOD("Fast food"),
    BAKERY("Bakery"),
    BAR("Bar")
}

data class Spot(
    val id: String,
    val name: String,
    val category: Category,
    val rating: Double,
    val priceLevel: Int,
    val lat: Double,
    val lng: Double,
    val openNow: Boolean,
    val photoUrl: String,
    val description: String,
    val updatedAt: Long
)

data class Review(
    val id: String,
    val spotId: String,
    val author: String,
    val stars: Int,
    val text: String,
    val createdAt: Long
)
