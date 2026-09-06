package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor
import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiTest {
    @Test
    fun `supports standard colors and attributes independently`() {
        val input = "\u001B[1;34mBold blue"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.DARK_BLUE}${ChatColor.BOLD}Bold blue",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `supports selective attribute resets while retaining color`() {
        val input = "\u001B[31;1;3mFirst\u001B[22mSecond\u001B[23mThird"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.BOLD}${ChatColor.ITALIC}First" +
                "${ChatColor.RESET}${ChatColor.DARK_RED}${ChatColor.ITALIC}Second" +
                "${ChatColor.RESET}${ChatColor.DARK_RED}Third",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `restores the supplied default color`() {
        val input = "Normal \u001B[31mred\u001B[39m normal"

        assertEquals(
            "${ChatColor.GRAY}Normal ${ChatColor.RESET}${ChatColor.DARK_RED}red" +
                "${ChatColor.RESET}${ChatColor.GRAY} normal",
            Ansi.toMinecraft(input, ChatColor.GRAY)
        )
    }

    @Test
    fun `converts semicolon ANSI true color`() {
        val input = "\u001B[38;2;255;128;0mOrange"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.of("#ff8000")}Orange",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `converts ISO colon ANSI true color with omitted color space`() {
        val input = "\u001B[38:2::31:1:38mCustom"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.of("#1f0126")}Custom",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `converts semicolon and colon 256 color forms`() {
        val input = "\u001B[38;5;196mRed \u001B[38:5:46mGreen"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.of("#ff0000")}Red " +
                "${ChatColor.RESET}${ChatColor.of("#00ff00")}Green",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `recognizes the eight-bit CSI representation`() {
        assertEquals(
            "${ChatColor.RESET}${ChatColor.GREEN}Green",
            Ansi.toMinecraft("\u009B92mGreen")
        )
    }

    @Test
    fun `treats an empty parameter as reset`() {
        val input = "\u001B[31;;1mBold default"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.BOLD}Bold default",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `ignores invalid extended colors without applying their components`() {
        val input = "\u001B[32mGreen\u001B[38;2;999;0;0m still green"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.DARK_GREEN}Green" +
                "${ChatColor.RESET}${ChatColor.DARK_GREEN} still green",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `removes unsupported terminal controls while preserving text`() {
        val input = "before\u001B[2Kafter \u001B]8;;https://example.com\u001B\\link\u001B]8;;\u001B\\"

        assertEquals("beforeafter link", Ansi.toMinecraft(input))
    }

    @Test
    fun `expands terminal tabs for Minecraft chat`() {
        assertEquals("    at Example.run", Ansi.toMinecraft("\tat Example.run"))
    }

    @Test
    fun `retains embedded Minecraft formatting when applying ANSI styles`() {
        val input = "${ChatColor.GREEN}Green \u001B[1mBold"

        assertEquals(
            "${ChatColor.GRAY}${ChatColor.GREEN}Green " +
                "${ChatColor.RESET}${ChatColor.GREEN}${ChatColor.BOLD}Bold",
            Ansi.toMinecraft(input, ChatColor.GRAY)
        )
    }

    @Test
    fun `ignores overflowing SGR parameters without resetting formatting`() {
        val input = "\u001B[31mRed\u001B[999999999999m still red"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.DARK_RED}Red" +
                "${ChatColor.RESET}${ChatColor.DARK_RED} still red",
            Ansi.toMinecraft(input)
        )
    }

    @Test
    fun `supports blink using Minecraft magic formatting`() {
        val input = "\u001B[5mMagic\u001B[25m normal"

        assertEquals(
            "${ChatColor.RESET}${ChatColor.MAGIC}Magic${ChatColor.RESET} normal",
            Ansi.toMinecraft(input)
        )
    }
}
