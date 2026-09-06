package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

object LegacyText {
    fun splitLines(text: String, maxLength: Int): List<String> {
        require(maxLength >= MINIMUM_LINE_LENGTH) {
            "maxLength must accommodate a formatting prefix and an atomic token"
        }

        val formatting = FormattingState()
        val lines = mutableListOf<String>()
        var line = StringBuilder()
        var lineHasVisibleText = false
        var logicalLineHasVisibleText = false
        var pendingContinuation = false
        var index = 0

        fun startLine(isContinuation: Boolean) {
            line = StringBuilder(formatting.prefix())
            lineHasVisibleText = false
            pendingContinuation = isContinuation
        }

        fun finishExplicitLine() {
            if (lineHasVisibleText || !pendingContinuation || !logicalLineHasVisibleText) {
                lines += line.toString()
            }
            startLine(isContinuation = false)
            logicalLineHasVisibleText = false
        }

        fun finishWrappedLine() {
            if (lineHasVisibleText) {
                lines += line.toString()
            }
            startLine(isContinuation = true)
        }

        fun appendVisibleToken(token: String) {
            if (line.length + token.length <= maxLength) {
                line.append(token)
                lineHasVisibleText = true
                logicalLineHasVisibleText = true
                return
            }

            // A grapheme cluster can be arbitrarily large (for example, a
            // base character followed by many combining marks). Split only
            // this pathological case into code points so the hard line limit
            // remains enforceable. Ordinary clusters are always kept intact.
            finishWrappedLine()
            var tokenIndex = 0
            while (tokenIndex < token.length) {
                val codePointLength = Character.charCount(token.codePointAt(tokenIndex))
                val codePoint = token.substring(tokenIndex, tokenIndex + codePointLength)
                if (line.length + codePoint.length > maxLength) {
                    finishWrappedLine()
                }
                line.append(codePoint)
                lineHasVisibleText = true
                logicalLineHasVisibleText = true
                tokenIndex += codePointLength
            }
        }

        while (index < text.length) {
            when {
                text[index] == '\r' || text[index] == '\n' -> {
                    if (text[index] == '\r' && text.getOrNull(index + 1) == '\n') index++
                    finishExplicitLine()
                    index++
                }

                text[index] == ChatColor.COLOR_CHAR -> {
                    val code = formattingCodeAt(text, index)
                    if (code != null) {
                        if (line.length + code.length > maxLength) {
                            finishWrappedLine()
                            formatting.apply(code)
                            line = StringBuilder(formatting.prefix())
                        } else {
                            line.append(code)
                            formatting.apply(code)
                        }
                        index += code.length
                    } else {
                        val token = textTokenAt(text, index)
                        appendVisibleToken(token)
                        index += token.length
                    }
                }

                else -> {
                    val token = textTokenAt(text, index)
                    appendVisibleToken(token)
                    index += token.length
                }
            }
        }

        if (lineHasVisibleText || !pendingContinuation || !logicalLineHasVisibleText) {
            // Formatting-only input still represents one logical line. Keep
            // its final canonical state rather than returning no lines after
            // a formatting prefix overflowed. A pending prefix after visible
            // text in this logical line remains a suppressed spill.
            lines += line.toString()
        }
        return lines
    }

    private fun textTokenAt(text: String, index: Int): String {
        val matcher = GRAPHEME_PATTERN.matcher(text)
        matcher.region(index, text.length)
        check(matcher.lookingAt()) { "Expected a grapheme at index $index" }
        return matcher.group()
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

    private fun isHexColor(value: String): Boolean {
        if (value.length != HEX_COLOR_LENGTH || value[1].lowercaseChar() != 'x') {
            return false
        }

        var index = 2
        while (index < HEX_COLOR_LENGTH) {
            if (value[index] != ChatColor.COLOR_CHAR || value[index + 1].digitToIntOrNull(16) == null) {
                return false
            }
            index += 2
        }
        return true
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

        fun prefix(): String {
            return buildString {
                append(color)
                for (style in styles) {
                    append(ChatColor.COLOR_CHAR)
                    append(style)
                }
            }
        }
    }

    private const val HEX_COLOR_LENGTH = 14
    private const val MAX_FORMATTING_PREFIX_LENGTH = HEX_COLOR_LENGTH + 10
    private const val MAX_ATOMIC_TOKEN_LENGTH = HEX_COLOR_LENGTH
    private const val MINIMUM_LINE_LENGTH = MAX_FORMATTING_PREFIX_LENGTH + MAX_ATOMIC_TOKEN_LENGTH
    private const val COLOR_CODES = "0123456789abcdef"
    private const val STYLE_CODES = "klmno"
    private const val LEGACY_CODES = COLOR_CODES + STYLE_CODES + "rx"
    private val GRAPHEME_PATTERN: Pattern = Pattern.compile("\\X")
}
