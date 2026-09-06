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
        val message = "abcdefghijklmnopqrstuvwxyz0123456789"
        val lines = LegacyText.splitLines("$color$message", 40)

        assertTrue(lines.size > 1)
        assertTrue(lines.all { it.startsWith(color) })
        assertEquals(message, lines.joinToString("") { ChatColor.stripColor(it) })
    }

    @Test
    fun `never divides a legacy hex color sequence`() {
        val color = ChatColor.of("#abcdef").toString()
        val prefix = "123456789012345678901234567890"
        val lines = LegacyText.splitLines("$prefix${color}colored", 38)

        assertEquals(prefix, lines.first())
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

    @Test
    fun `keeps supplementary Unicode characters intact at boundaries`() {
        val message = "a".repeat(999) + "😀"
        val lines = LegacyText.splitLines(message, 1_000)

        assertEquals(listOf("a".repeat(999), "😀"), lines)
        assertEquals(message, lines.joinToString(""))
    }

    @Test
    fun `continuation prefixes never exceed the line limit`() {
        val formatting = ChatColor.of("#abcdef").toString() +
            ChatColor.MAGIC + ChatColor.BOLD + ChatColor.STRIKETHROUGH +
            ChatColor.UNDERLINE + ChatColor.ITALIC
        val message = "123456789012345"

        val lines = LegacyText.splitLines(formatting + message, 38)

        assertTrue(lines.all { it.length <= 38 })
        assertTrue(lines.all { ChatColor.stripColor(it).orEmpty().isNotEmpty() })
        assertEquals(message, lines.joinToString("") { ChatColor.stripColor(it) })
    }
}
