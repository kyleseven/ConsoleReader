package me.kyleseven.consolereader.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

// ANSI order differs from Minecraft's legacy color order.
private val ANSI_COLORS = listOf(
    NamedTextColor.BLACK, NamedTextColor.DARK_RED, NamedTextColor.DARK_GREEN, NamedTextColor.GOLD,
    NamedTextColor.DARK_BLUE, NamedTextColor.DARK_PURPLE, NamedTextColor.DARK_AQUA, NamedTextColor.GRAY,
    NamedTextColor.DARK_GRAY, NamedTextColor.RED, NamedTextColor.GREEN, NamedTextColor.YELLOW,
    NamedTextColor.BLUE, NamedTextColor.LIGHT_PURPLE, NamedTextColor.AQUA, NamedTextColor.WHITE
)
private val LEGACY_COLORS = listOf(
    NamedTextColor.BLACK, NamedTextColor.DARK_BLUE, NamedTextColor.DARK_GREEN, NamedTextColor.DARK_AQUA,
    NamedTextColor.DARK_RED, NamedTextColor.DARK_PURPLE, NamedTextColor.GOLD, NamedTextColor.GRAY,
    NamedTextColor.DARK_GRAY, NamedTextColor.BLUE, NamedTextColor.GREEN, NamedTextColor.AQUA,
    NamedTextColor.RED, NamedTextColor.LIGHT_PURPLE, NamedTextColor.YELLOW, NamedTextColor.WHITE
)
private val CUBE_LEVELS = intArrayOf(0, 95, 135, 175, 215, 255)

// Resolves palette, color-cube, and grayscale entries in the 256-color table.
private fun indexedColor(index: Int): TextColor? = when (index) {
    in 0..15 -> ANSI_COLORS[index]
    in 16..231 -> {
        val cube = index - 16
        TextColor.color(CUBE_LEVELS[cube / 36], CUBE_LEVELS[cube / 6 % 6], CUBE_LEVELS[cube % 6])
    }
    in 232..255 -> {
        val gray = 8 + 10 * (index - 232)
        TextColor.color(gray, gray, gray)
    }
    else -> null
}

/** Parses log formatting only; unsupported terminal controls do not affect the output. */
fun parseANSI(message: String, defaultColor: TextColor = NamedTextColor.GRAY): Component {
    if ('\u001B' !in message && '§' !in message) {
        return Component.text(message, defaultColor)
    }
    var color = defaultColor
    val decorations = mutableSetOf<TextDecoration>()
    val output = Component.text()
    val text = StringBuilder()

    // Emits buffered text with explicit decoration states to prevent inheritance.
    fun flush() {
        if (text.isEmpty()) return
        val segment = Component.text(text.toString(), color).toBuilder()
        for (decoration in TextDecoration.entries) {
            segment.decoration(decoration, decoration in decorations)
        }
        output.append(segment.build())
        text.setLength(0)
    }

    // Clears decorations and restores the message default unless a color is supplied.
    fun reset(newColor: TextColor = defaultColor) {
        color = newColor
        decorations.clear()
    }

    // Applies semicolon-separated formatting codes, consuming color operands together.
    fun applySgr(parameters: String) {
        // Colon syntax, private parameters, and intermediate bytes are unsupported.
        if (parameters.any { it !in '0'..'9' && it != ';' }) return
        val parametersList = parameters.split(';')
        var index = 0
        while (index < parametersList.size) {
            val parameter = parametersList[index]
            val code = if (parameter.isEmpty()) 0 else parameter.toIntOrNull()
            when (code) {
                0 -> reset()
                1 -> decorations.add(TextDecoration.BOLD)
                3 -> decorations.add(TextDecoration.ITALIC)
                4 -> decorations.add(TextDecoration.UNDERLINED)
                9 -> decorations.add(TextDecoration.STRIKETHROUGH)
                22 -> decorations.remove(TextDecoration.BOLD)
                23 -> decorations.remove(TextDecoration.ITALIC)
                24 -> decorations.remove(TextDecoration.UNDERLINED)
                29 -> decorations.remove(TextDecoration.STRIKETHROUGH)
                in 30..37 -> color = ANSI_COLORS[code!! - 30]
                in 90..97 -> color = ANSI_COLORS[code!! - 90 + 8]
                39 -> color = defaultColor
                38, 48 -> {
                    val mode = parametersList.getOrNull(index + 1)?.toIntOrNull()
                    val operandCount = when (mode) {
                        5 -> 1
                        2 -> 3
                        else -> return
                    }
                    if (index + 1 + operandCount >= parametersList.size) return
                    // Empty color operands are invalid, rather than an ordinary SGR reset.
                    val operands = (1..operandCount).map {
                        parametersList[index + 1 + it].toIntOrNull()
                    }
                    if (code == 38 && operands.all { it != null && it in 0..255 }) {
                        color = if (mode == 5) {
                            indexedColor(operands[0]!!)!!
                        } else {
                            TextColor.color(operands[0]!!, operands[1]!!, operands[2]!!)
                        }
                    }
                    index += operandCount + 1
                }
            }
            index++
        }
    }

    var index = 0
    while (index < message.length) {
        val character = message[index]
        if (character == '§' && index + 1 < message.length) {
            val code = message[index + 1].lowercaseChar()
            if (code == 'x' && index + 13 < message.length) {
                val valid = (0..5).all {
                    message[index + 2 + it * 2] == '§' &&
                        message[index + 3 + it * 2].lowercaseChar() in "0123456789abcdef"
                }
                if (valid) {
                    val hex = (0..5).joinToString("") { message[index + 3 + it * 2].toString() }
                    flush()
                    reset(TextColor.color(hex.toInt(16)))
                    index += 14
                    continue
                }
            }
            val legacyColor = "0123456789abcdef".indexOf(code)
            val decoration = when (code) {
                'k' -> TextDecoration.OBFUSCATED
                'l' -> TextDecoration.BOLD
                'm' -> TextDecoration.STRIKETHROUGH
                'n' -> TextDecoration.UNDERLINED
                'o' -> TextDecoration.ITALIC
                else -> null
            }
            if (legacyColor >= 0 || decoration != null || code == 'r') {
                flush()
                when {
                    legacyColor >= 0 -> reset(LEGACY_COLORS[legacyColor])
                    decoration != null -> decorations.add(decoration)
                    else -> reset()
                }
                index += 2
                continue
            }
        }
        if (character == '\u001B') {
            when (message.getOrNull(index + 1)) {
                '[' -> {
                    var end = index + 2
                    while (end < message.length && message[end] in '0'..'?') end++
                    while (end < message.length && message[end] in ' '..'/') end++
                    if (end < message.length && message[end] in '@'..'~') {
                        if (message[end] == 'm') {
                            flush()
                            applySgr(message.substring(index + 2, end))
                        }
                        index = end + 1
                        continue
                    }
                }
                ']' -> {
                    var end = index + 2
                    while (end < message.length) {
                        if (message[end] == '\u0007') break
                        if (message[end] == '\u001B' && message.getOrNull(end + 1) == '\\') break
                        end++
                    }
                    if (end < message.length) {
                        index = end + if (message[end] == '\u0007') 1 else 2
                        continue
                    }
                }
            }
            // A malformed or unfinished sequence loses only ESC, not the remaining text.
            index++
            continue
        }
        text.append(character)
        index++
    }
    flush()
    return output.build()
}
