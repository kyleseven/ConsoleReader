package me.kyleseven.consolereader.logreader

import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

internal object LogFilters {
    private const val PLAYER_NAME_PLACEHOLDER = "%PLAYERNAME%"

    fun compile(filters: List<String>, playerName: String): List<Pattern> = filters.mapNotNull { filter ->
        try {
            Pattern.compile(filter.replace(PLAYER_NAME_PLACEHOLDER, playerName))
        } catch (_: PatternSyntaxException) {
            null
        }
    }

    fun matches(filters: List<Pattern>, message: String): Boolean =
        filters.any { it.matcher(message).matches() }
}
