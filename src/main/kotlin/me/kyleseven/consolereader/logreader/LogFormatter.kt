package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.parseANSI
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// A log line ready for chat. plainMessage is the message without any formatting, used for the regex filters.
class FormattedLine(val component: BaseComponent, val plainMessage: String)

object LogFormatter {
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())

    fun format(line: LogLine): FormattedLine {
        // Log components
        val instant = Instant.ofEpochMilli(line.timeMillis)
        val logDate = DATE_FORMATTER.format(instant)
        val logTime = TIME_FORMATTER.format(instant)
        val logLevel = line.level.toString()
        val loggerName = line.loggerName.ifBlank { "None" }
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

        // Adding color to WARN, FATAL, and ERROR messages.
        val logColor: ChatColor = when (logLevel) {
            "WARN" -> ChatColor.YELLOW
            "FATAL", "ERROR" -> ChatColor.RED
            else -> MainConfig.logColor
        }

        // Creating Hover Text
        val hoverText = ComponentBuilder("")
            .append("Time: ").color(ChatColor.GRAY).append("$logDate $logTime\n").color(ChatColor.WHITE)
            .append("Log Level: ").color(ChatColor.GRAY).append(logLevel + "\n").color(ChatColor.WHITE)
            .append("Logger: ").color(ChatColor.GRAY).append(loggerName + "\n").color(ChatColor.WHITE)
            .append("Thread: ").color(ChatColor.GRAY).append(line.threadName).color(ChatColor.WHITE)

        val chatLogPrefix = TextComponent(messagePrefix)
        chatLogPrefix.color = logColor
        chatLogPrefix.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hoverText.build()))
        val chatLogMessage = parseANSI(line.message, logColor)

        return FormattedLine(TextComponent(chatLogPrefix, chatLogMessage), chatLogMessage.toPlainText())
    }
}
