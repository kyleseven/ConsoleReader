package me.kyleseven.consolereader.config

import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

object MainConfig : ConfigLoader("config.yml") {
    /*
    Internal
     */
    private val _regexFilters: MutableList<String> = mutableListOf()
    private var _logColor: ChatColor = ChatColor.GRAY

    /*
    Config keys
     */
    val version: String
        get() {
            val default = "INVALID"
            return config.getString("version") ?: default
        }

    val prefix: String
        get() {
            val default = "&8[&3CR&8] &7"
            return config.getString("prefix") ?: default
        }

    val logColor: ChatColor
        get() = _logColor

    val forbiddenCommands: List<String>
        get() = config.getStringList("forbidden_commands")

    val regexFilters: List<String>
        get() = _regexFilters

    init {
        parseLogColor()
        validateRegexPatterns()
    }

    // The log color is used for every log line, so it is only parsed when the config is loaded.
    private fun parseLogColor() {
        val default = ChatColor.GRAY
        val colorString = config.getString("log_color")?.trim() ?: "GRAY"

        val color: ChatColor? = try {
            if (colorString.length == 1) {
                ChatColor.getByChar(colorString[0].lowercaseChar())
            } else {
                ChatColor.of(colorString)
            }
        } catch (e: Exception) {
            null
        }

        // Formatting codes like BOLD are not colors
        _logColor = if (color?.color != null) color else default
    }

    private fun validateRegexPatterns() {
        val validRegexFilters: MutableList<Pattern> = mutableListOf()

        validRegexFilters.addAll(
            config.getStringList("filters").mapNotNull {
                try {
                    Pattern.compile(it)
                } catch (e: Exception) {
                    null
                }
            }
        )

        _regexFilters.clear()
        _regexFilters.addAll(validRegexFilters.map { it.pattern() })
    }

    fun reload() {
        config = loadConfig()
        parseLogColor()
        validateRegexPatterns()
    }
}
