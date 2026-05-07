package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.parseANSI
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.apache.logging.log4j.core.LogEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class LogMessage(val prefix: TextComponent, val body: TextComponent)

object LogMessageBuilder {
    private val yellowColor = ChatColor.YELLOW.toString()
    private val redColor = ChatColor.RED.toString()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

    fun build(log: LogEvent): LogMessage {
        var logMessage = log.message.formattedMessage
        val logLevel = log.level.toString()
        val loggerName = log.loggerName.ifBlank { "None" }

        val instant = Instant.ofEpochMilli(log.timeMillis)
        val logDate = dateFormatter.format(instant)
        val logTime = timeFormatter.format(instant)
        val threadName = log.threadName

        val prefixBuilder = StringBuilder()
        prefixBuilder.append("[$logTime $logLevel]: ")

        /*
        When using Spigot, logger name is already included in the log message.
        When using Paper, the logger name will need to be added here.
         */
        if (ConsoleReader.instance.isPaperMC) {
            if (!(loggerName.contains("net.minecraft") || loggerName == "Minecraft" || loggerName == "None")) {
                prefixBuilder.append("[$loggerName] ")
            }
        }

        val logColorStr = MainConfig.logColor.toString()
        val colorCode = when (logLevel) {
            "WARN" -> yellowColor
            "FATAL", "ERROR" -> redColor
            else -> logColorStr
        }

        val messagePrefix = colorCode + prefixBuilder.toString()
        logMessage = colorCode + parseANSI(logMessage)

        val chatLogPrefix = TextComponent(TextComponent.fromLegacy(messagePrefix))
        val chatLogMessage = TextComponent(TextComponent.fromLegacy(logMessage))
        val hoverText = ComponentBuilder("")
            .append("Time: ").color(ChatColor.GRAY).append("$logDate $logTime\n").color(ChatColor.WHITE)
            .append("Log Level: ").color(ChatColor.GRAY).append("$logLevel\n").color(ChatColor.WHITE)
            .append("Logger: ").color(ChatColor.GRAY).append("$loggerName\n").color(ChatColor.WHITE)
            .append("Thread: ").color(ChatColor.GRAY).append(threadName).color(ChatColor.WHITE)
        chatLogPrefix.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverText.create()))

        return LogMessage(chatLogPrefix, chatLogMessage)
    }
}