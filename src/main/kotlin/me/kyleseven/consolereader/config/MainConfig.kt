package me.kyleseven.consolereader.config

import me.kyleseven.consolereader.ConsoleReader
import net.md_5.bungee.api.ChatColor
import java.util.regex.Pattern

object MainConfig : ConfigLoader("config.yml") {
    /*
    Internal
     */
    @Volatile
    private var _regexFilters: List<String> = emptyList()

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
        _regexFilters = config.getStringList("filters").mapNotNull { filter ->
            try {
                Pattern.compile(filter).pattern()
            } catch (exception: IllegalArgumentException) {
                ConsoleReader.instance.logger.warning(
                    "Ignoring invalid console filter '$filter': ${exception.message}"
                )
                null
            }
        }
    }

    fun reload() {
        config = loadConfig()
        validateRegexPatterns()
    }
}
