package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import java.awt.Color

private const val ESC = '\u001B'

// ANSI colors 0-7 as (normal, bright) Minecraft colors
private val ansiColors = arrayOf(
    ChatColor.BLACK to ChatColor.DARK_GRAY,
    ChatColor.DARK_RED to ChatColor.RED,
    ChatColor.DARK_GREEN to ChatColor.GREEN,
    ChatColor.GOLD to ChatColor.YELLOW,
    ChatColor.DARK_BLUE to ChatColor.BLUE,
    ChatColor.DARK_PURPLE to ChatColor.LIGHT_PURPLE,
    ChatColor.DARK_AQUA to ChatColor.AQUA,
    ChatColor.GRAY to ChatColor.WHITE
)

// Color from the 256 color table
private fun indexedColor(index: Int): ChatColor? {
    return when (index) {
        in 0..7 -> ansiColors[index].first
        in 8..15 -> ansiColors[index - 8].second
        in 16..231 -> {
            val cube = index - 16
            val levels = intArrayOf(0, 95, 135, 175, 215, 255)
            ChatColor.of(Color(levels[cube / 36], levels[cube / 6 % 6], levels[cube % 6]))
        }

        in 232..255 -> {
            val gray = 8 + (index - 232) * 10
            ChatColor.of(Color(gray, gray, gray))
        }

        else -> null
    }
}

/*
Converts a console message into a chat component.
- ANSI color and style sequences (SGR) are translated to Minecraft colors and formatting.
- Section sign color codes are translated too, since plugin loggers leave them in the message.
- All other escape sequences are removed.
Text without a color, or text after a reset, uses baseColor.
 */
fun parseANSI(message: String, baseColor: ChatColor): BaseComponent {
    val result = TextComponent("")
    val run = StringBuilder()
    var i = 0

    // Current style. A null color means baseColor.
    var color: ChatColor? = null
    var basicColor: Int? = null
    var intense = false
    val formats = mutableSetOf<ChatColor>()

    // Adds the text collected so far using the current style
    fun flush() {
        if (run.isEmpty()) {
            return
        }

        val component = TextComponent(run.toString())
        val basic = basicColor
        component.color = if (basic != null) {
            if (intense) ansiColors[basic].second else ansiColors[basic].first
        } else {
            color ?: baseColor
        }

        /*
        Spigot writes bright colors as a basic color with code 1, i.e. "0;32;1" for green.
        So code 1 only means bold when there is no basic color for it to brighten.
         */
        if (ChatColor.BOLD in formats || (intense && basic == null)) {
            component.isBold = true
        }
        if (ChatColor.ITALIC in formats) {
            component.isItalic = true
        }
        if (ChatColor.UNDERLINE in formats) {
            component.isUnderlined = true
        }
        if (ChatColor.STRIKETHROUGH in formats) {
            component.isStrikethrough = true
        }
        if (ChatColor.MAGIC in formats) {
            component.isObfuscated = true
        }

        result.addExtra(component)
        run.setLength(0)
    }

    fun setColor(newColor: ChatColor?) {
        // Code 1 was only brightening the basic color being replaced
        if (basicColor != null) {
            intense = false
        }
        basicColor = null
        color = newColor
    }

    fun resetStyle() {
        color = null
        basicColor = null
        intense = false
        formats.clear()
    }

    /*
    Reads the arguments of code 38, 48, or 58. Either "38;5;n" and "38;2;r;g;b", or the colon forms "38:5:n" and "38:2::r:g:b".
    Returns the color and how many of the following codes were used.
     */
    fun readExtendedColor(parts: List<String>, codes: List<String>, k: Int): Pair<ChatColor?, Int> {
        val colonForm = parts.size > 1
        val args = if (colonForm) parts.drop(1) else codes.subList(k + 1, codes.size)

        return when (args.firstOrNull()?.toIntOrNull()) {
            5 -> {
                val indexed = args.getOrNull(1)?.toIntOrNull()?.let { indexedColor(it) }
                indexed to if (colonForm) 0 else 2
            }

            2 -> {
                // The colon form can have a color space id before the color values
                val rgb = if (colonForm && args.size >= 5) args.subList(2, 5) else args.drop(1).take(3)
                val values = rgb.mapNotNull { it.toIntOrNull() }.filter { it in 0..255 }
                val trueColor = if (values.size == 3) ChatColor.of(Color(values[0], values[1], values[2])) else null
                trueColor to if (colonForm) 0 else 4
            }

            else -> null to if (colonForm) 0 else 1
        }
    }

    fun applySGR(params: String) {
        // Private sequences like "ESC[>4;2m" are not styling
        if (params.isNotEmpty() && params[0] in "<=>?") {
            return
        }

        val codes = params.split(';')
        var k = 0
        while (k < codes.size) {
            val parts = codes[k].split(':')
            when (val code = parts[0].toIntOrNull() ?: 0) {
                0 -> resetStyle()
                1 -> intense = true
                22 -> {
                    intense = false
                    formats.remove(ChatColor.BOLD)
                }

                3 -> formats.add(ChatColor.ITALIC)
                23 -> formats.remove(ChatColor.ITALIC)
                4 -> formats.add(ChatColor.UNDERLINE)
                24 -> formats.remove(ChatColor.UNDERLINE)
                9 -> formats.add(ChatColor.STRIKETHROUGH)
                29 -> formats.remove(ChatColor.STRIKETHROUGH)

                // Spigot writes bold as code 21
                21 -> formats.add(ChatColor.BOLD)

                // Spigot writes magic as code 5 (blink) and Paper writes it as code 8 (conceal)
                5, 8 -> formats.add(ChatColor.MAGIC)
                25, 28 -> formats.remove(ChatColor.MAGIC)

                in 30..37 -> {
                    basicColor = code - 30
                    color = null
                }

                in 90..97 -> setColor(ansiColors[code - 90].second)
                39 -> setColor(null)
                38 -> {
                    val (extendedColor, used) = readExtendedColor(parts, codes, k)
                    if (extendedColor != null) {
                        setColor(extendedColor)
                    }
                    k += used
                }

                // Background and underline colors can't be shown in chat, but their arguments need to be skipped
                48, 58 -> k += readExtendedColor(parts, codes, k).second
            }
            k++
        }
    }

    fun parseEscape() {
        when (message.getOrNull(i + 1)) {
            '[' -> {
                // Parameters go on until a final character between '@' and '~'
                var end = i + 2
                while (end < message.length && message[end] !in '@'..'~') {
                    end++
                }

                if (end < message.length && message[end] == 'm') {
                    flush()
                    applySGR(message.substring(i + 2, end))
                }
                i = minOf(end + 1, message.length)
            }

            ']' -> {
                // Operating system commands end with BEL or "ESC\"
                var end = i + 2
                while (end < message.length) {
                    if (message[end] == '\u0007') {
                        end++
                        break
                    }
                    if (message[end] == ESC && message.getOrNull(end + 1) == '\\') {
                        end += 2
                        break
                    }
                    end++
                }
                i = end
            }

            null -> i++
            else -> i += 2
        }
    }

    // Reads the "§r§r§g§g§b§b" part of a "§x" hex color
    fun readLegacyHex(start: Int): ChatColor? {
        if (start + 12 > message.length) {
            return null
        }

        val hex = StringBuilder("#")
        for (k in 0 until 6) {
            val position = start + k * 2
            if (message[position] != ChatColor.COLOR_CHAR || message[position + 1].digitToIntOrNull(16) == null) {
                return null
            }
            hex.append(message[position + 1])
        }

        return ChatColor.of(hex.toString())
    }

    fun parseLegacy() {
        val code = message[i + 1].lowercaseChar()
        val hexColor = if (code == 'x') readLegacyHex(i + 2) else null
        val legacy: ChatColor? = if (code == 'x') null else ChatColor.getByChar(code)

        when {
            hexColor != null -> {
                flush()
                resetStyle()
                color = hexColor
                i += 14
            }

            legacy == null -> {
                // Not a color code, keep the character
                run.append(message[i])
                i++
            }

            else -> {
                flush()
                when {
                    legacy == ChatColor.RESET -> resetStyle()
                    legacy.color != null -> {
                        // A color code also clears formatting
                        resetStyle()
                        color = legacy
                    }

                    else -> formats.add(legacy)
                }
                i += 2
            }
        }
    }

    while (i < message.length) {
        val c = message[i]
        when (c) {
            ESC -> parseEscape()
            ChatColor.COLOR_CHAR if i + 1 < message.length -> parseLegacy()
            '\t' -> {
                // Chat can't show tabs
                run.append("    ")
                i++
            }

            '\r' -> i++
            else -> {
                run.append(c)
                i++
            }
        }
    }
    flush()

    return result
}
