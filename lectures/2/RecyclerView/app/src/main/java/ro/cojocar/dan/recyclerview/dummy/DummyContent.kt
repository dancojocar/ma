package ro.cojocar.dan.recyclerview.dummy

import java.util.*

object DummyContent {

  val ITEMS: MutableList<DummyItem> = ArrayList()

  val ITEM_MAP: MutableMap<String, DummyItem> = HashMap()

  private const val COUNT = 25

  init {
    // Add some sample items.
    for (i in 1..COUNT) {
      addItem(createDummyItem(i))
    }
  }

  private fun addItem(item: DummyItem) {
    ITEMS.add(item)
    ITEM_MAP[item.id] = item
  }

  private fun createDummyItem(position: Int): DummyItem {
    return DummyItem(position.toString(), "Item $position", makeDetails(position))
  }

  private fun makeDetails(position: Int): String {
    val builder = StringBuilder()
    builder.append("Details about Item: ").append(position)
    for (i in 0 until position) {
      builder.append("\nMore details information here.")
    }
    return builder.toString()
  }

  data class DummyItem(val id: String, val content: String, val details: String) {
    override fun toString(): String = content
  }
}
