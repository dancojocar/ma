package com.unieats.app.ui.components

import com.unieats.shared.Spot
import java.util.Locale

fun Spot.ratingText(): String = String.format(Locale.US, "%.1f", rating)

fun Spot.priceText(): String = "$".repeat(priceLevel)
