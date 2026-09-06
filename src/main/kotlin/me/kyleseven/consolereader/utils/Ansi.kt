package me.kyleseven.consolereader.utils

import net.md_5.bungee.api.ChatColor

/** Translates terminal SGR styling into Minecraft legacy chat formatting. */
object Ansi {
    fun toMinecraft(message: String, defaultColor: ChatColor? = null): String {
        val state = RenditionState(defaultColor)
        val translated = buildString {
            defaultColor?.let(::append)
            var endOfLastMatch = 0
            for (match in SGR_REGEX.findAll(message)) {
                append(message, endOfLastMatch, match.range.first)
                state.apply(match.groupValues[1])
                append(state.asMinecraftFormatting())
                endOfLastMatch = match.range.last + 1
            }
            append(message, endOfLastMatch, message.length)
        }
        return translated
            .replace("\t", TAB_SPACES)
            .replace(OSC_REGEX, "")
            .replace(CSI_REGEX, "")
            .replace(TWO_CHARACTER_ESCAPE_REGEX, "")
            .replace(UNSUPPORTED_CONTROL_REGEX, "")
    }

    private class RenditionState(private val defaultColor: ChatColor?) {
        private var foreground = defaultColor
        private var bold = false
        private var italic = false
        private var underlined = false
        private var strikethrough = false

        fun apply(encodedParameters: String) {
            val parameters = encodedParameters.split(';')
            var index = 0
            while (index < parameters.size) {
                val parameter = parameters[index]
                if (':' in parameter) {
                    applyColonParameter(parameter)
                    index++
                    continue
                }

                when (val code = parameter.toIntOrNull() ?: 0) {
                    0 -> reset()
                    1 -> bold = true
                    3 -> italic = true
                    4, 21 -> underlined = true
                    9 -> strikethrough = true
                    22 -> bold = false
                    23 -> italic = false
                    24 -> underlined = false
                    29 -> strikethrough = false
                    in 30..37 -> foreground = STANDARD_COLORS[code - 30]
                    38 -> {
                        val consumedParameters = semicolonExtendedColorParameterCount(parameters, index)
                        parseSemicolonExtendedColor(parameters, index)?.let { parsed ->
                            foreground = parsed.color
                        }
                        index += consumedParameters
                    }
                    39 -> foreground = defaultColor
                    in 90..97 -> foreground = BRIGHT_COLORS[code - 90]
                    48, 58 -> index += semicolonExtendedColorParameterCount(parameters, index)
                }
                index++
            }
        }

        fun asMinecraftFormatting(): String = buildString {
            append(ChatColor.RESET)
            foreground?.let(::append)
            if (bold) append(ChatColor.BOLD)
            if (italic) append(ChatColor.ITALIC)
            if (underlined) append(ChatColor.UNDERLINE)
            if (strikethrough) append(ChatColor.STRIKETHROUGH)
        }

        private fun reset() {
            foreground = defaultColor
            bold = false
            italic = false
            underlined = false
            strikethrough = false
        }

        private fun applyColonParameter(parameter: String) {
            val parts = parameter.split(':')
            when (parts.firstOrNull()?.toIntOrNull()) {
                4 -> underlined = parts.getOrNull(1)?.toIntOrNull() != 0
                38 -> parseColonExtendedColor(parts)?.let { foreground = it }
                // Background and underline colors have no Minecraft chat equivalent.
                48, 58 -> Unit
            }
        }
    }

    private data class ParsedColor(val color: ChatColor)

    private fun parseSemicolonExtendedColor(parameters: List<String>, index: Int): ParsedColor? {
        return when (parameters.getOrNull(index + 1)?.toIntOrNull()) {
            2 -> rgbColor(
                parameters.getOrNull(index + 2)?.toIntOrNull(),
                parameters.getOrNull(index + 3)?.toIntOrNull(),
                parameters.getOrNull(index + 4)?.toIntOrNull()
            )?.let(::ParsedColor)
            5 -> indexedColor(parameters.getOrNull(index + 2)?.toIntOrNull())
                ?.let(::ParsedColor)
            else -> null
        }
    }

    private fun parseColonExtendedColor(parts: List<String>): ChatColor? {
        return when (parts.getOrNull(1)?.toIntOrNull()) {
            2 -> {
                val rgbStart = if (parts.size >= 6) 3 else 2
                rgbColor(
                    parts.getOrNull(rgbStart)?.toIntOrNull(),
                    parts.getOrNull(rgbStart + 1)?.toIntOrNull(),
                    parts.getOrNull(rgbStart + 2)?.toIntOrNull()
                )
            }
            5 -> indexedColor(parts.getOrNull(2)?.toIntOrNull())
            else -> null
        }
    }

    private fun rgbColor(red: Int?, green: Int?, blue: Int?): ChatColor? {
        if (red !in COLOR_COMPONENT_RANGE || green !in COLOR_COMPONENT_RANGE || blue !in COLOR_COMPONENT_RANGE) {
            return null
        }
        return ChatColor.of("#%02x%02x%02x".format(red, green, blue))
    }

    private fun indexedColor(index: Int?): ChatColor? {
        val validIndex = index?.takeIf { it in ANSI_COLOR_INDEX_RANGE } ?: return null
        if (validIndex < ANSI_16_COLORS.size) return ChatColor.of(ANSI_16_COLORS[validIndex])

        if (validIndex < 232) {
            val cubeIndex = validIndex - 16
            return rgbColor(
                ANSI_CUBE_LEVELS[cubeIndex / 36],
                ANSI_CUBE_LEVELS[(cubeIndex / 6) % 6],
                ANSI_CUBE_LEVELS[cubeIndex % 6]
            )
        }

        val gray = 8 + (validIndex - 232) * 10
        return rgbColor(gray, gray, gray)
    }

    private fun semicolonExtendedColorParameterCount(parameters: List<String>, index: Int): Int =
        when (parameters.getOrNull(index + 1)?.toIntOrNull()) {
            2 -> 4
            5 -> 2
            else -> 0
        }

    private val STANDARD_COLORS = arrayOf(
        ChatColor.BLACK, ChatColor.DARK_RED, ChatColor.DARK_GREEN, ChatColor.GOLD,
        ChatColor.DARK_BLUE, ChatColor.DARK_PURPLE, ChatColor.DARK_AQUA, ChatColor.GRAY
    )
    private val BRIGHT_COLORS = arrayOf(
        ChatColor.DARK_GRAY, ChatColor.RED, ChatColor.GREEN, ChatColor.YELLOW,
        ChatColor.BLUE, ChatColor.LIGHT_PURPLE, ChatColor.AQUA, ChatColor.WHITE
    )
    private val ANSI_16_COLORS = listOf(
        "#000000", "#800000", "#008000", "#808000",
        "#000080", "#800080", "#008080", "#c0c0c0",
        "#808080", "#ff0000", "#00ff00", "#ffff00",
        "#0000ff", "#ff00ff", "#00ffff", "#ffffff"
    )
    private val ANSI_CUBE_LEVELS = intArrayOf(0, 95, 135, 175, 215, 255)
    private const val TAB_SPACES = "    "
    private val COLOR_COMPONENT_RANGE = 0..255
    private val ANSI_COLOR_INDEX_RANGE = 0..255
    private val SGR_REGEX = "(?:\u001B\\[|\u009B)([0-9;:]*)m".toRegex()
    private val CSI_REGEX = "(?:\u001B\\[|\u009B)[0-?]*[ -/]*[@-~]".toRegex()
    private val OSC_REGEX = "(?:\u001B]|\u009D).*?(?:\u0007|\u001B\\\\|\u009C)"
        .toRegex(RegexOption.DOT_MATCHES_ALL)
    private val TWO_CHARACTER_ESCAPE_REGEX = "\u001B[ -/]*[0-~]".toRegex()
    private val UNSUPPORTED_CONTROL_REGEX =
        "[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F-\u009F]".toRegex()
}
