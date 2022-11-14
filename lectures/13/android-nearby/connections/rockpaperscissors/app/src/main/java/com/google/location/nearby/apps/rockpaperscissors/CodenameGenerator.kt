package com.google.location.nearby.apps.rockpaperscissors

import java.util.*

/**
 * Utility class to generate random Android names
 */
object CodenameGenerator {
  private val COLORS = arrayOf(
      "Red",
      "Orange",
      "Yellow",
      "Green",
      "Blue",
      "Indigo",
      "Violet",
      "Purple",
      "Lavender",
      "Fuchsia",
      "Plum",
      "Orchid",
      "Magenta")
  private val TREATS = arrayOf(
      "Alpha",
      "Beta",
      "Cupcake",
      "Donut",
      "Eclair",
      "Froyo",
      "Gingerbread",
      "Honeycomb",
      "Ice Cream Sandwich",
      "Jellybean",
      "Kit Kat",
      "Lollipop",
      "Marshmallow",
      "Nougat",
      "Oreo",
      "Pie"
  )
  private val generator = Random()
  /**
   * Generate a random Android agent codename
   */
  fun generate(): String {
    val color = COLORS[generator.nextInt(COLORS.size)]
    val treat = TREATS[generator.nextInt(TREATS.size)]
    return "$color $treat"
  }
}