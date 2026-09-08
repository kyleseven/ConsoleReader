package me.kyleseven.consolereader.config

import me.kyleseven.consolereader.logreader.LogAppenderManager
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import java.util.Locale
import java.util.regex.Pattern

object MainConfig : ConfigLoader("config.yml") {
    /*
    Internal
     */
    data class RegexFilter(val source: String, val pattern: Pattern?, val playerDependent: Boolean)

    private var _logColor: TextColor = NamedTextColor.GRAY
    private var _regexFilters: List<RegexFilter> = emptyList()

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

    val logColor: TextColor
        get() = _logColor

    val forbiddenCommands: List<String>
        get() = config.getStringList("forbidden_commands")

    val regexFilters: List<RegexFilter>
        get() = _regexFilters

    init {
        refreshCachedValues()
    }

    // Parses colors and validates filter templates once per configuration load.
    private fun refreshCachedValues() {
        val value = config.getString("log_color")
        _logColor = if (value == null) NamedTextColor.GRAY else when {
            value.length == 1 -> LegacyComponentSerializer.legacySection().deserialize("§$value ").color()
            value.startsWith("#") -> TextColor.fromHexString(value)
            else -> NamedTextColor.NAMES.value(value.lowercase(Locale.ROOT))
        } ?: NamedTextColor.GRAY
        _regexFilters = config.getStringList("filters").mapNotNull { source ->
            try {
                RegexFilter(source, Pattern.compile(source), source.contains("%PLAYERNAME%"))
            } catch (_: Exception) {
                null
            }
        }
    }

    fun reload() {
        config = loadConfig()
        refreshCachedValues()
        LogAppenderManager.invalidateConfigCaches()
    }
}
