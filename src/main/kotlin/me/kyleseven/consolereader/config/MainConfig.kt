package me.kyleseven.consolereader.config

import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

object MainConfig : ConfigLoader("config.yml") {
    /*
    Internal
     */
    private val _regexFilters: MutableList<String> = mutableListOf()

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
        get() {
            val default = "GRAY"
            val colorString = config.getString("log_color") ?: default

            return try {
                if (colorString.length == 1) {
                    ChatColor.getByChar(colorString[0])
                } else {
                    ChatColor.of(colorString)
                }
            } catch (e: Exception) {
                ChatColor.of(default)
            }
        }

    val forbiddenCommands: List<String>
        get() = config.getStringList("forbidden_commands")

    val regexFilters: List<String>
        get() = _regexFilters

    init {
        validateRegexPatterns()
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
        validateRegexPatterns()
    }
}