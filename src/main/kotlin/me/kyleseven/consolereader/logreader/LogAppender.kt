package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.parseANSI
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.bukkit.entity.Player
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class LogAppender : AbstractAppender("ConsoleReader", null, null, false, null) {
    private val playerFilters = mutableMapOf<UUID, Map<String, Pattern>>()

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    }

    override fun append(event: LogEvent?) {
        if (event != null) {
            LogAppenderManager.enqueue(event)
        }
    }

    // Filters on the server thread before building shared presentation for recipients.
    internal fun deliver(log: LogEvent, players: List<Player>) {
        if (players.isEmpty()) {
            return
        }

        val logLevel = log.level.toString()
        val logColor = when (logLevel) {
            "WARN" -> NamedTextColor.YELLOW
            "ERROR", "FATAL" -> NamedTextColor.RED
            else -> MainConfig.logColor
        }
        val logMessage = parseANSI(log.message.formattedMessage, logColor)
        val filters = MainConfig.regexFilters
        val hasPlayerFilters = filters.any { it.playerDependent }
        if (hasPlayerFilters) {
            for (player in players) {
                playerFilters.getOrPut(player.uniqueId) { filtersFor(player) }
            }
        }
        val hasUsableFilters = filters.any { !it.playerDependent && it.pattern != null } ||
            (hasPlayerFilters && players.any { playerFilters[it.uniqueId]?.isNotEmpty() == true })
        val recipients: List<Player>
        if (hasUsableFilters) {
            val plainMessage = PlainTextComponentSerializer.plainText().serialize(logMessage)
            val unfilteredPlayers = mutableListOf<Player>()
            for (player in players) {
                val playerPatterns = playerFilters[player.uniqueId].orEmpty()
                var filtered = false
                for ((source, pattern1, playerDependent) in filters) {
                    val pattern = if (playerDependent) {
                        playerPatterns[source]
                    } else {
                        pattern1
                    }
                    if (pattern != null && pattern.matcher(plainMessage).matches()) {
                        filtered = true
                        break
                    }
                }
                if (!filtered) {
                    unfilteredPlayers.add(player)
                }
            }
            recipients = unfilteredPlayers
        } else {
            recipients = players
        }
        if (recipients.isEmpty()) {
            return
        }

        val loggerName = log.loggerName?.ifBlank { "None" } ?: "None"
        val instant = Instant.ofEpochMilli(log.timeMillis)
        val logDate = DATE_FORMATTER.format(instant)
        val logTime = TIME_FORMATTER.format(instant)
        var messagePrefix = "[$logTime $logLevel]: "
        if (!(loggerName.contains("net.minecraft") || loggerName == "Minecraft" || loggerName == "None")) {
            messagePrefix += "[$loggerName] "
        }
        val hoverText = Component.text()
            .append(Component.text("Time: ", NamedTextColor.GRAY))
            .append(Component.text("$logDate $logTime\n", NamedTextColor.WHITE))
            .append(Component.text("Log Level: ", NamedTextColor.GRAY))
            .append(Component.text("$logLevel\n", NamedTextColor.WHITE))
            .append(Component.text("Logger: ", NamedTextColor.GRAY))
            .append(Component.text("$loggerName\n", NamedTextColor.WHITE))
            .append(Component.text("Thread: ", NamedTextColor.GRAY))
            .append(Component.text(log.threadName ?: "None", NamedTextColor.WHITE))
            .build()
        val chatLogMessage = Component.text()
            .append(Component.text(messagePrefix, logColor).hoverEvent(hoverText))
            .append(logMessage)
            .build()

        for (player in recipients) {
            player.sendMessage(chatLogMessage)
        }
    }

    // Compiles player substitutions for caching, omitting invalid expressions.
    private fun filtersFor(player: Player): Map<String, Pattern> = MainConfig.regexFilters
        .filter { it.playerDependent }
        .mapNotNull { filter ->
            try {
                filter.source to Pattern.compile(filter.source.replace("%PLAYERNAME%", player.name))
            } catch (_: PatternSyntaxException) {
                null
            }
        }
        .toMap()

    internal fun clearFilterCache(uuid: UUID) {
        playerFilters.remove(uuid)
    }

    internal fun clearFilterCache() {
        playerFilters.clear()
    }
}
