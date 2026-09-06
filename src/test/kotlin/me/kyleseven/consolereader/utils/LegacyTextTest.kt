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
    fun `does not emit a formatting-only line when formatting overflows`() {
        val message = "a".repeat(999) + ChatColor.RED

        val lines = LegacyText.splitLines(message, 1_000)

        assertEquals(listOf("a".repeat(999)), lines)
    }

    @Test
    fun `folds repeated formatting codes without empty wrapped lines`() {
        val input = ChatColor.BOLD.toString().repeat(20) + "x"

        val lines = LegacyText.splitLines(input, 38)

        assertEquals(listOf("${ChatColor.BOLD}x"), lines)
    }

    @Test
    fun `preserves a dangling color character after a pending continuation`() {
        val input = ChatColor.BOLD.toString().repeat(20) + ChatColor.COLOR_CHAR

        val lines = LegacyText.splitLines(input, 38)

        assertEquals(listOf("${ChatColor.BOLD}${ChatColor.COLOR_CHAR}"), lines)
    }

    @Test
    fun `returns one canonical line for formatting-only input`() {
        val input = ChatColor.BOLD.toString().repeat(20)

        val lines = LegacyText.splitLines(input, 38)

        assertEquals(listOf(ChatColor.BOLD.toString()), lines)
    }

    @Test
    fun `preserves formatting-only logical line after a newline`() {
        val input = "a\n" + ChatColor.BOLD.toString().repeat(20)

        val lines = LegacyText.splitLines(input, 38)

        assertEquals(listOf("a", ChatColor.BOLD.toString()), lines)
    }

    @Test
    fun `preserves formatting-only logical line before following text`() {
        val input = ChatColor.BOLD.toString().repeat(20) + "\nnext"

        val lines = LegacyText.splitLines(input, 38)

        assertEquals(listOf(ChatColor.BOLD.toString(), "${ChatColor.BOLD}next"), lines)
    }

    @Test
    fun `does not emit a pending formatting continuation before a newline`() {
        val input = "a".repeat(999) + ChatColor.RED + "\nnext"

        val lines = LegacyText.splitLines(input, 1_000)

        assertEquals(
            listOf("a".repeat(999), "${ChatColor.RED}next"),
            lines
        )
    }

    @Test
    fun `preserves explicit blank logical lines`() {
        assertEquals(listOf("a", "", "b"), LegacyText.splitLines("a\n\nb", 38))
    }

    @Test
    fun `keeps combining marks in one grapheme`() {
        val grapheme = "e\u0301"
        val lines = LegacyText.splitLines("a".repeat(37) + grapheme, 38)

        assertEquals(listOf("a".repeat(37), grapheme), lines)
    }

    @Test
    fun `keeps emoji modifiers in one grapheme`() {
        val grapheme = "👍🏽"
        val lines = LegacyText.splitLines("a".repeat(37) + grapheme, 38)

        assertEquals(listOf("a".repeat(37), grapheme), lines)
    }

    @Test
    fun `keeps zwj emoji sequences in one grapheme`() {
        val grapheme = "👩‍💻"
        val lines = LegacyText.splitLines("a".repeat(37) + grapheme, 38)

        assertEquals(listOf("a".repeat(37), grapheme), lines)
    }

    @Test
    fun `keeps variation selectors in one grapheme`() {
        val grapheme = "☹️"
        val lines = LegacyText.splitLines("a".repeat(37) + grapheme, 38)

        assertEquals(listOf("a".repeat(37), grapheme), lines)
    }

    @Test
    fun `splits an oversized grapheme by code point without exceeding the limit`() {
        val color = ChatColor.of("#abcdef").toString()
        val grapheme = "a" + "\u0301".repeat(30)
        val lines = LegacyText.splitLines(color + grapheme, 38)

        assertTrue(lines.all { it.length <= 38 })
        assertTrue(lines.all { ChatColor.stripColor(it).orEmpty().isNotEmpty() })
        assertEquals(grapheme, lines.joinToString("") { ChatColor.stripColor(it).orEmpty() })
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
