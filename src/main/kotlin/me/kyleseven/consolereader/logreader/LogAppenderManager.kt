package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.Ansi
import me.kyleseven.consolereader.utils.LegacyText
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.Logger
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import java.util.regex.Pattern

object LogAppenderManager {
    private data class Subscription(
        var persistent: Boolean,
        var filters: List<Pattern>,
        var expirationTask: BukkitTask? = null
    )

    private lateinit var logger: Logger
    private lateinit var appender: LogAppender
    private var drainTask: BukkitTask? = null
    private val subscriptions = mutableMapOf<UUID, Subscription>()

    fun setup(logger: Logger) {
        this.logger = logger
        subscriptions.clear()

        appender = LogAppender()
        appender.start()
        logger.addAppender(appender)
        drainTask = Bukkit.getScheduler().runTaskTimer(
            ConsoleReader.instance,
            Runnable(::drainLogs),
            1L,
            1L
        )
    }

    fun getReadingPlayerUUIDs(): List<UUID> = subscriptions
        .filterValues(Subscription::persistent)
        .keys
        .toList()

    fun isReading(player: OfflinePlayer): Boolean = subscriptions.containsKey(player.uniqueId)

    fun startReading(player: OfflinePlayer) {
        subscriptions[player.uniqueId]?.expirationTask?.cancel()
        subscriptions[player.uniqueId] = Subscription(
            persistent = true,
            filters = compileFilters(player.name.orEmpty())
        )
    }

    fun startReadingTemp(player: Player, seconds: Int) {
        if (subscriptions.containsKey(player.uniqueId)) return

        val subscription = Subscription(false, compileFilters(player.name))
        subscriptions[player.uniqueId] = subscription
        subscription.expirationTask = Bukkit.getScheduler().runTaskLater(
            ConsoleReader.instance,
            Runnable { removeTemporarySubscription(player.uniqueId) },
            seconds.coerceAtLeast(0) * 20L
        )
    }

    fun stopReading(player: OfflinePlayer) {
        subscriptions.remove(player.uniqueId)?.expirationTask?.cancel()
    }

    fun stopReadingTemp(player: Player) {
        removeTemporarySubscription(player.uniqueId)
    }

    fun reloadFilters() {
        subscriptions.forEach { (uuid, subscription) ->
            subscription.filters = compileFilters(Bukkit.getOfflinePlayer(uuid).name.orEmpty())
        }
    }

    fun stopReadingAll() {
        drainTask?.cancel()
        drainTask = null
        subscriptions.values.forEach { it.expirationTask?.cancel() }
        subscriptions.clear()

        if (::appender.isInitialized) {
            logger.removeAppender(appender)
            appender.stop()
            appender.clear()
        }
    }

    private fun removeTemporarySubscription(uuid: UUID) {
        val subscription = subscriptions[uuid] ?: return
        if (!subscription.persistent) {
            subscription.expirationTask?.cancel()
            subscriptions.remove(uuid)
        }
    }

    private fun drainLogs() {
        val result = appender.drain(MAX_EVENTS_PER_TICK)
        if (result.droppedEntries > 0) {
            broadcastNotice("[ConsoleReader] ${result.droppedEntries} console messages omitted.")
        }
        result.entries.forEach(::deliver)
    }

    private fun deliver(entry: LogAppender.Entry) {
        val messageColor = when (entry.level) {
            Level.WARN -> ChatColor.YELLOW
            Level.ERROR, Level.FATAL -> ChatColor.RED
            else -> MainConfig.logColor
        }
        val instant = Instant.ofEpochMilli(entry.timeMillis)
        val date = DATE_FORMATTER.format(instant)
        val time = TIME_FORMATTER.format(instant)
        val loggerPrefix = if (
            ConsoleReader.instance.isPaperMC &&
            !entry.loggerName.contains("net.minecraft") &&
            entry.loggerName != "Minecraft" &&
            entry.loggerName != "None"
        ) "[${entry.loggerName}] " else ""
        val prefix = "$messageColor[$time ${entry.level}]: $loggerPrefix"
        val hover = ComponentBuilder("")
            .append("Time: ").color(ChatColor.GRAY).append("$date $time\n").color(ChatColor.WHITE)
            .append("Log Level: ").color(ChatColor.GRAY).append("${entry.level}\n").color(ChatColor.WHITE)
            .append("Logger: ").color(ChatColor.GRAY).append("${entry.loggerName}\n").color(ChatColor.WHITE)
            .append("Thread: ").color(ChatColor.GRAY).append(entry.threadName).color(ChatColor.WHITE)
            .create()

        val parsedMessage = Ansi.toMinecraft(entry.message, messageColor)
        val filterMessage = ChatColor.stripColor(parsedMessage).orEmpty()
        val lines = buildLines(parsedMessage, entry.throwable)

        subscriptions.forEach { (uuid, subscription) ->
            val player = Bukkit.getPlayer(uuid)?.takeIf(Player::isOnline) ?: return@forEach
            if (LogFilters.matches(subscription.filters, filterMessage)) return@forEach
            lines.forEach { player.sendLogLine(prefix, it, hover) }
        }
    }

    private fun Player.sendLogLine(prefix: String, line: String, hover: Array<BaseComponent>) {
        val prefixComponent = TextComponent(TextComponent.fromLegacy(prefix)).apply {
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hover))
        }
        spigot().sendMessage(prefixComponent, TextComponent(TextComponent.fromLegacy(line)))
    }

    private fun broadcastNotice(message: String) {
        subscriptions.keys.mapNotNull(Bukkit::getPlayer).forEach { player ->
            if (player.isOnline) player.sendMessage(message)
        }
    }

    private fun buildLines(message: String, throwable: String?): List<String> {
        val messageLines = LegacyText.splitLines(message, MAX_CHARACTERS_PER_LINE)
        val throwableLines = throwable
            ?.let { Ansi.toMinecraft(it, ChatColor.RED) }
            ?.let { LegacyText.splitLines(it, MAX_CHARACTERS_PER_LINE) }
            .orEmpty()
        return messageLines + throwableLines
    }

    private fun compileFilters(playerName: String): List<Pattern> =
        LogFilters.compile(MainConfig.regexFilters, playerName)

    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
    private const val MAX_EVENTS_PER_TICK = 100
    private const val MAX_CHARACTERS_PER_LINE = 1_000
}
