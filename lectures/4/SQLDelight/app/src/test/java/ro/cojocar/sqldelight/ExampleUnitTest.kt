package ro.cojocar.sqldelight

import com.squareup.sqldelight.sqlite.driver.JdbcSqliteDriver
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val inMemorySqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).apply {
        Database.Schema.create(this)
    }

    private val queries = Database(inMemorySqlDriver).playerQueries

    @Test
    fun smokeTest() {
        val emptyItems = queries.selectAll().executeAsList()
        assertEquals(emptyItems.size, 1)

        queries.insertPlayer(
            player_number = 10L,
            full_name = "Bobby Fischer",
            quotes = "I like the moment when I break a man's ego."
        )

        val players = queries.selectAll().executeAsList()
        assertEquals(players.size, 2)

        val player = queries.selectByName("Bobby Fischer").executeAsOneOrNull()
        assertEquals(player?.full_name, "Bobby Fischer")
        assertEquals(player?.player_number, 10L)
    }
}