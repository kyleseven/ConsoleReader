package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FormattingPipelineTest {
    @Test
    fun `ANSI reset crossing the length boundary does not create an empty continuation`() {
        val visibleMessage = "x".repeat(994)
        val parsedMessage = Ansi.toMinecraft(
            "\u001B[31m$visibleMessage\u001B[0m",
            ChatColor.GRAY
        )

        val lines = LegacyText.splitLines(parsedMessage, 1_000)

        assertTrue(lines.all { it.length <= 1_000 })
        assertTrue(lines.all { ChatColor.stripColor(it).orEmpty().isNotEmpty() })
        assertEquals(
            visibleMessage,
            lines.joinToString("") { ChatColor.stripColor(it).orEmpty() }
        )
    }
}
