package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.config.MainConfig
import org.bukkit.entity.Player
import java.util.regex.PatternSyntaxException

class LogReader(val player: Player) {
    // Time in milliseconds when a temporary reader stops reading. Null if the reader doesn't expire.
    var expiresAt: Long? = null

    private var filters: List<Regex> = listOf()

    init {
        compileFilters()
    }

    // %PLAYERNAME% depends on the reader, so each reader has their own compiled filters.
    fun compileFilters() {
        filters = MainConfig.regexFilters.mapNotNull {
            try {
                it.replace("%PLAYERNAME%", player.name).toRegex()
            } catch (e: PatternSyntaxException) {
                // Regex filters are validated at config load, so we shouldn't reach this point.
                null
            }
        }
    }

    fun isFiltered(message: String): Boolean {
        return filters.any { it.matches(message) }
    }

    fun isExpired(): Boolean {
        val expiresAt = expiresAt ?: return false
        return System.currentTimeMillis() >= expiresAt
    }
}
