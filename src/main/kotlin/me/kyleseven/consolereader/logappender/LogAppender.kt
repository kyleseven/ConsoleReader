package me.kyleseven.consolereader.logappender

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.parseANSI
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.apache.commons.lang.time.DateFormatUtils
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.bukkit.entity.Player

@Suppress("DEPRECATION") // Paper/Spigot uses an older version of Log4J that does not have the new constructor
class LogAppender(private val player: Player) : AbstractAppender("ConsoleReader-${player.uniqueId}", null, null, false) {
    init {
        start()
    }

    override fun append(event: LogEvent?) {
        val log = event!!.toImmutable()

        // Log components
        val logColor: ChatColor = MainConfig.logColor
        var logMessage = log.message.formattedMessage
        val logDate = DateFormatUtils.format(log.timeMillis, "yyyy-MM-dd")
        val logTime = DateFormatUtils.format(log.timeMillis, "HH:mm:ss")
        val logLevel = log.level.toString()
        val loggerName = log.loggerName.ifBlank { "None" }
        val threadName = log.threadName
        var messagePrefix = "[$logTime $logLevel]: "

        /*
        When using Spigot, logger name is already included in the log message.
        When using Paper, the logger name will need to be added here.
         */
        if (ConsoleReader.instance.isPaperMC) {
            if (!(loggerName.contains("net.minecraft") || loggerName == "Minecraft" || loggerName == "None")) {
                messagePrefix += "[$loggerName] "
            }
        }

        if (logLevel == "WARN") {
            messagePrefix = ChatColor.YELLOW.toString() + messagePrefix
            logMessage = ChatColor.YELLOW.toString() + logMessage
        } else if (logLevel == "FATAL" || logLevel == "ERROR") {
            messagePrefix = ChatColor.RED.toString() + messagePrefix
            logMessage = ChatColor.RED.toString() + logMessage
        } else {
            messagePrefix = logColor.toString() + messagePrefix
            logMessage = logColor.toString() + logMessage
        }

        logMessage = parseANSI(logMessage)

        /*
        Filtering console messages here.
        - Go through regex filter.
        - Showing logger name if it is not from the game itself.
        - Adding color to WARN, FATAL, and ERROR messages.
         */
        for (regexString in MainConfig.regexFilters) {
            val strippedMsg = ChatColor.stripColor(logMessage)
            val regexToMatch = regexString.replace("%PLAYERNAME%", player.name).toRegex()
            if (regexToMatch.matches(strippedMsg)) {
                return
            }
        }

        // Creating Hover Text
        val chatLogPrefix = TextComponent(*TextComponent.fromLegacyText(messagePrefix))
        val chatLogMessage = TextComponent(*TextComponent.fromLegacyText(logMessage))
        val hoverText = ComponentBuilder("")
                .append("Time: ").color(ChatColor.GRAY).append("$logDate $logTime\n").color(ChatColor.WHITE)
                .append("Log Level: ").color(ChatColor.GRAY).append(logLevel.trimIndent() + "\n").color(ChatColor.WHITE)
                .append("Logger: ").color(ChatColor.GRAY).append(loggerName.trimIndent() + "\n").color(ChatColor.WHITE)
                .append("Thread: ").color(ChatColor.GRAY).append(threadName).color(ChatColor.WHITE)
        chatLogPrefix.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create())

        player.spigot().sendMessage(chatLogPrefix, chatLogMessage)
    }
}