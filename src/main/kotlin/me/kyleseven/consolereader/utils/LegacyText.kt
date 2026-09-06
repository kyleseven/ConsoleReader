package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor

/** Utilities for safely dividing Minecraft legacy-formatted text into chat messages. */
object LegacyText {
    /**
     * Splits text at newlines and length boundaries without cutting formatting codes.
     * Active colors and styles are repeated at the beginning of every continuation.
     */
    fun splitLines(text: String, maxLength: Int): List<String> {
        require(maxLength >= MAX_FORMATTING_PREFIX_LENGTH) {
            "maxLength must accommodate a complete legacy formatting prefix"
        }

        val formatting = FormattingState()
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        var index = 0

        fun finishLine() {
            lines += line.toString()
            line = StringBuilder(formatting.prefix())
        }

        while (index < text.length) {
            when {
                text[index] == '\r' || text[index] == '\n' -> {
                    if (text[index] == '\r' && text.getOrNull(index + 1) == '\n') index++
                    finishLine()
                    index++
                }

                text[index] == ChatColor.COLOR_CHAR -> {
                    val code = formattingCodeAt(text, index)
                    if (code != null) {
                        if (line.isNotEmpty() && line.length + code.length > maxLength) finishLine()
                        line.append(code)
                        formatting.apply(code)
                        index += code.length
                    } else {
                        appendCharacter(line, formatting, lines, text[index], maxLength).also { line = it }
                        index++
                    }
                }

                else -> {
                    appendCharacter(line, formatting, lines, text[index], maxLength).also { line = it }
                    index++
                }
            }
        }

        lines += line.toString()
        return lines
    }

    private fun appendCharacter(
        currentLine: StringBuilder,
        formatting: FormattingState,
        completedLines: MutableList<String>,
        character: Char,
        maxLength: Int
    ): StringBuilder {
        var line = currentLine
        if (line.length == maxLength) {
            completedLines += line.toString()
            line = StringBuilder(formatting.prefix())
        }
        line.append(character)
        return line
    }

    private fun formattingCodeAt(text: String, start: Int): String? {
        val code = text.getOrNull(start + 1)?.lowercaseChar() ?: return null
        if (code == 'x') {
            val end = start + HEX_COLOR_LENGTH
            val candidate = text.substring(start, end.coerceAtMost(text.length))
            return candidate.takeIf(::isHexColor)
        }
        return text.substring(start, start + 2).takeIf { code in LEGACY_CODES }
    }

    private fun isHexColor(value: String): Boolean =
        value.length == HEX_COLOR_LENGTH &&
            value[1].lowercaseChar() == 'x' &&
            (2 until HEX_COLOR_LENGTH step 2).all { index ->
                value[index] == ChatColor.COLOR_CHAR && value[index + 1].digitToIntOrNull(16) != null
            }

    private class FormattingState {
        private var color = ""
        private val styles = linkedSetOf<Char>()

        fun apply(code: String) {
            val type = code[1].lowercaseChar()
            when (type) {
                'x', in COLOR_CODES -> {
                    color = code
                    styles.clear()
                }
                'r' -> {
                    color = ""
                    styles.clear()
                }
                in STYLE_CODES -> styles += type
            }
        }

        fun prefix(): String = buildString {
            append(color)
            styles.forEach { style ->
                append(ChatColor.COLOR_CHAR)
                append(style)
            }
        }
    }

    private const val HEX_COLOR_LENGTH = 14
    private const val MAX_FORMATTING_PREFIX_LENGTH = HEX_COLOR_LENGTH + 10
    private const val COLOR_CODES = "0123456789abcdef"
    private const val STYLE_CODES = "klmno"
    private const val LEGACY_CODES = COLOR_CODES + STYLE_CODES + "rx"
}
