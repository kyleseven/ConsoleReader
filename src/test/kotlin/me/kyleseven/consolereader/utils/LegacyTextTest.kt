package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTextTest {
    @Test
    fun `carries color and styles across newlines`() {
        val input = "${ChatColor.RED}${ChatColor.BOLD}first\nsecond"

        assertEquals(
            listOf(
                "${ChatColor.RED}${ChatColor.BOLD}first",
                "${ChatColor.RED}${ChatColor.BOLD}second"
            ),
            LegacyText.splitLines(input, 64)
        )
    }

    @Test
    fun `carries hex colors across length boundaries`() {
        val color = ChatColor.of("#12abef").toString()
        val lines = LegacyText.splitLines("${color}abcdefghijklmnopqrstuvwxyz", 24)

        assertTrue(lines.size > 1)
        assertTrue(lines.all { it.startsWith(color) })
        assertEquals("abcdefghijklmnopqrstuvwxyz", lines.joinToString("") { ChatColor.stripColor(it) })
    }

    @Test
    fun `never divides a legacy hex color sequence`() {
        val color = ChatColor.of("#abcdef").toString()
        val lines = LegacyText.splitLines("1234567890${color}colored", 24)

        assertEquals("1234567890${color}", lines.first())
        assertTrue(lines[1].startsWith(color))
        assertTrue(lines.none { it.endsWith(ChatColor.COLOR_CHAR) })
    }

    @Test
    fun `color and reset codes clear inherited styles`() {
        val input = "${ChatColor.BOLD}bold${ChatColor.BLUE}blue\nstill blue${ChatColor.RESET}\nplain"

        assertEquals(
            listOf(
                "${ChatColor.BOLD}bold${ChatColor.BLUE}blue",
                "${ChatColor.BLUE}still blue${ChatColor.RESET}",
                "plain"
            ),
            LegacyText.splitLines(input, 64)
        )
    }
}
