package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor
import kotlin.test.Test
import kotlin.test.assertEquals

class AnsiParserTest {
    // Checks the parsed message as a list of "text|color|formatting"
    private fun assertParsed(message: String, vararg expected: String) {
        val parsed = parseANSI(message, ChatColor.GRAY)
        val actual = parsed.extra.orEmpty().map {
            val formats = mutableListOf<String>()
            if (it.isBoldRaw == true) formats.add("bold")
            if (it.isItalicRaw == true) formats.add("italic")
            if (it.isUnderlinedRaw == true) formats.add("underline")
            if (it.isStrikethroughRaw == true) formats.add("strikethrough")
            if (it.isObfuscatedRaw == true) formats.add("magic")

            "${it.toPlainText()}|${it.color.name}|${formats.joinToString(",")}"
        }

        assertEquals(expected.toList(), actual)
        assertEquals(expected.joinToString("") { it.substringBefore('|') }, parsed.toPlainText())
    }

    /*
    Messages captured from a Spigot 26.3 server, as log4j gives them to appenders
     */
    @Test
    fun `captured console sender colors`() {
        assertParsed(
            "T02 [0;32;1mGreen [0;32;22mDarkGreen [0;33;22mGold [0;30;1mDarkGray [0;30;22mBlack [0;37;1mWhite [0;37;22mGray [0;34;1mBlue [mreset[m",
            "T02 |gray|", "Green |green|", "DarkGreen |dark_green|", "Gold |gold|", "DarkGray |dark_gray|",
            "Black |black|", "White |white|", "Gray |gray|", "Blue |blue|", "reset|gray|"
        )
    }

    @Test
    fun `captured console sender formatting`() {
        assertParsed(
            "T03 [0;31;1mRed [21mBold [3mItalic [4mUnder [9mStrike [5mMagic [mreset[m",
            "T03 |gray|", "Red |red|", "Bold |red|bold", "Italic |red|bold,italic", "Under |red|bold,italic,underline",
            "Strike |red|bold,italic,underline,strikethrough", "Magic |red|bold,italic,underline,strikethrough,magic",
            "reset|gray|"
        )
    }

    @Test
    fun `captured console sender hex colors`() {
        assertParsed(
            "T04 [38;2;255;136;0morange [0;32;1mGreen [38;2;1;136;254mblue[m",
            "T04 |gray|", "orange |#ff8800|", "Green |green|", "blue|#0188fe|"
        )
    }

    @Test
    fun `captured console sender component`() {
        // Spigot does not reset formatting before a hex color, so bold carries on like it does in a terminal
        assertParsed(
            "[0;37;1m[0;36;1mT05 component [0;33;22m[21mbold gold[38;2;18;171;52m hex[m",
            "T05 component |aqua|", "bold gold|gold|bold", " hex|#12ab34|bold"
        )
    }

    @Test
    fun `captured plugin prefix message`() {
        assertParsed(
            "[0;37;1m[0;30;1m[[0;36;22mCR[0;30;1m] [0;37;22mConsoleReader 1.5.6 by kyleseven[m",
            "[|dark_gray|", "CR|dark_aqua|", "] |dark_gray|", "ConsoleReader 1.5.6 by kyleseven|gray|"
        )
    }

    @Test
    fun `captured plugin logger and stdout`() {
        assertParsed(
            "[CaptureTest] T01 plugin logger §aGreen §lBold§r reset",
            "[CaptureTest] T01 plugin logger |gray|", "Green |green|", "Bold|green|bold", " reset|gray|"
        )
        assertParsed("T06 stdout [31mraw red[0m plain", "T06 stdout |gray|", "raw red|dark_red|", " plain|gray|")
    }

    /*
    Spigot console sender
     */
    @Test
    fun `spigot bright colors are not bold`() {
        assertParsed(
            "\u001B[0;32;1mGreen\u001B[0;31;1mRed\u001B[m plain",
            "Green|green|", "Red|red|", " plain|gray|"
        )
    }

    @Test
    fun `spigot normal colors`() {
        assertParsed(
            "\u001B[0;32;22mDark Green\u001B[0;33;22mGold\u001B[0;30;1mDark Gray\u001B[m",
            "Dark Green|dark_green|", "Gold|gold|", "Dark Gray|dark_gray|"
        )
    }

    @Test
    fun `spigot formatting codes`() {
        assertParsed(
            "\u001B[0;33;22mGold\u001B[21mBold\u001B[3mItalic\u001B[m \u001B[5mMagic\u001B[9m\u001B[4mBoth\u001B[m",
            "Gold|gold|", "Bold|gold|bold", "Italic|gold|bold,italic", " |gray|", "Magic|gray|magic",
            "Both|gray|underline,strikethrough,magic"
        )
    }

    @Test
    fun `spigot hex color after a bright color is not bold`() {
        assertParsed(
            "\u001B[0;32;1mGreen\u001B[38;2;255;136;0mOrange\u001B[m",
            "Green|green|", "Orange|#ff8800|"
        )
    }

    /*
    Paper and plugins that write ANSI themselves
     */
    @Test
    fun `bold with a 256 color`() {
        assertParsed(
            "component \u001B[1m\u001B[38;5;208mhex\u001B[0m\u001B[38;5;14m named\u001B[0m",
            "component |gray|", "hex|#ff8700|bold", " named|aqua|"
        )
    }

    @Test
    fun `256 color table`() {
        assertParsed(
            "\u001B[38;5;1ma\u001B[38;5;9mb\u001B[38;5;16mc\u001B[38;5;231md\u001B[38;5;244me",
            "a|dark_red|", "b|red|", "c|#000000|", "d|#ffffff|", "e|#808080|"
        )
    }

    @Test
    fun `code 1 before or after a basic color is bright`() {
        assertParsed(
            "\u001B[1;32ma\u001B[0m\u001B[32m\u001B[1mb\u001B[22mc\u001B[39md",
            "a|green|", "b|green|", "c|dark_green|", "d|gray|"
        )
    }

    @Test
    fun `bright color codes`() {
        assertParsed("\u001B[91mRed\u001B[97mWhite", "Red|red|", "White|white|")
    }

    @Test
    fun `colon separated true color`() {
        assertParsed("\u001B[38:2::255:136:0ma\u001B[38:2:1:2:3mb\u001B[38:5:208mc", "a|#ff8800|", "b|#010203|", "c|#ff8700|")
    }

    @Test
    fun `background colors are skipped`() {
        assertParsed("\u001B[48;5;196;31ma\u001B[0;48;2;1;2;3;34mb", "a|dark_red|", "b|dark_blue|")
    }

    @Test
    fun `formatting can be turned off`() {
        assertParsed(
            "\u001B[3;4;9ma\u001B[23mb\u001B[24mc\u001B[29md",
            "a|gray|italic,underline,strikethrough", "b|gray|underline,strikethrough", "c|gray|strikethrough", "d|gray|"
        )
    }

    /*
    Section sign color codes
     */
    @Test
    fun `legacy color codes`() {
        assertParsed(
            "T02 §aGreen §lBold§r reset §x§f§f§8§8§0§0orange §LBOLD §cred",
            "T02 |gray|", "Green |green|", "Bold|green|bold", " reset |gray|", "orange |#ff8800|",
            "BOLD |#ff8800|bold", "red|red|"
        )
    }

    @Test
    fun `unknown legacy codes are kept`() {
        assertParsed("5 § 6 §z §x§1§2 §", "5 § 6 §z §x|gray|", " §|dark_green|")
    }

    /*
    Everything else
     */
    @Test
    fun `plain text`() {
        assertParsed("Done (3.141s)! For help, type \"help\"", "Done (3.141s)! For help, type \"help\"|gray|")
        assertParsed("")
    }

    @Test
    fun `other escape sequences are removed`() {
        assertParsed(
            "a\u001B[2Kb\u001B[1;1Hc\u001B]0;title\u0007d\u001B]8;;http://example.com\u001B\\e\u001B[>4;2mf\u001B7g",
            "abcde|gray|", "fg|gray|"
        )
    }

    @Test
    fun `unfinished escape sequences`() {
        assertParsed("a\u001B", "a|gray|")
        assertParsed("a\u001B[31", "a|gray|")
        assertParsed("a\u001B[38;5mb", "a|gray|", "b|gray|")
        assertParsed("a\u001B[38;2;255mb", "a|gray|", "b|gray|")
    }

    @Test
    fun `tabs and carriage returns`() {
        assertParsed("a\tb\r\nc", "a    b\nc|gray|")
    }
}
