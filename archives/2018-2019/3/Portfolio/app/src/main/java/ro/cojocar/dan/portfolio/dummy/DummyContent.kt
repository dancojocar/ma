package ro.cojocar.dan.portfolio.dummy

import ro.cojocar.dan.portfolio.domain.Portfolio
import java.util.*

/**
 * Helper class for providing sample content for user interfaces.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    val portfolios: MutableList<Portfolio> = ArrayList()

    /**
     * A map of sample (dummy) items, by ID.
     */
    val portfoliosMap: MutableMap<Long, Portfolio> = HashMap()

    private val COUNT = 10

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createDummyPortfolios(i.toLong()))
        }
    }

    private fun addItem(item: Portfolio) {
        portfolios.add(item)
        portfoliosMap.put(item.id, item)
    }

    private fun createDummyPortfolios(position: Long): Portfolio {
        return Portfolio(id = position, name = "Portfolio $position", lastModified = 0)
    }
}
