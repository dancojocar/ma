/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.activityscenetransitionbasic

/**
 * Represents an Item in our application. Each item has a name, id, full size image url and
 * thumbnail url.
 */
class Item internal constructor(val name: String, val author: String, private val mFileName: String) {

  val id: Int
    get() = name.hashCode() + mFileName.hashCode()

  val photoUrl: String
    get() = "$LARGE_BASE_URL$mFileName.jpg"

  val thumbnailUrl: String
    get() = "$LARGE_BASE_URL$mFileName.sm.jpg"

  companion object {

    private const val LARGE_BASE_URL = "https://www.gstatic.com/webp/gallery/"

    var ITEMS = arrayOf(
        Item("Nærøyfjorden, Norway", "Kjetil Birkeland Moe", "1"),
        Item("Kayaker at Ekstremsportveko 2010", "Kjetil Birkeland Moe", "2"),
        Item("Frame 10 of the Parkrun", "Lars Haglund", "3"),
        Item("A Wild Cherry", "Benjamin Gimmel", "4"),
        Item("Fire breathing", " Luc Viatour", "5")
    )

    fun getItem(id: Int): Item? {
      for (item in ITEMS) {
        if (item.id == id) {
          return item
        }
      }
      return null
    }
  }
}
