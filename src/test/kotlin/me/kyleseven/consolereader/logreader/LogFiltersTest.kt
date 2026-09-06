package me.kyleseven.consolereader.logreader

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LogFiltersTest {
    @Test
    fun `substitutes player names and uses full matches`() {
        val filters = LogFilters.compile(
            listOf("%PLAYERNAME% issued server command: .*"),
            "KyleSeven"
        )

        assertTrue(LogFilters.matches(filters, "KyleSeven issued server command: /help"))
        assertFalse(LogFilters.matches(filters, "OtherPlayer issued server command: /help"))
        assertFalse(LogFilters.matches(filters, "prefix KyleSeven issued server command: /help"))
    }

    @Test
    fun `ignores invalid patterns`() {
        assertTrue(LogFilters.compile(listOf("[", "valid.*"), "player").single().matcher("valid log").matches())
    }
}
