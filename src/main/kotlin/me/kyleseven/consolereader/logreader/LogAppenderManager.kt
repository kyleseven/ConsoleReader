package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import org.apache.logging.log4j.core.Logger
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CopyOnWriteArraySet

object LogAppenderManager {
    private lateinit var logger: Logger
    private lateinit var appender: LogAppender

    // Active online subscribers
    private val subscribers = CopyOnWriteArraySet<Player>()

    // UUIDs of all players who have console reading enabled (including offline players)
    private val readingUUIDs: MutableSet<UUID> = Collections.synchronizedSet(HashSet())

    // Drain task — only running while there is at least one subscriber
    private var drainTask: BukkitTask? = null

    fun setup(logger: Logger) {
        this.logger = logger
        LogFilterManager.load()
        appender = LogAppender(
            subscribers,
            { LogFilterManager.globalFilters },
            { uuid -> LogFilterManager.getPlayerFilters(uuid) }
        )
        logger.addAppender(appender)
    }

    private fun startDrainTaskIfNeeded() {
        if (drainTask != null) return
        drainTask = Bukkit.getScheduler().runTaskTimer(ConsoleReader.instance, Runnable {
            appender.drainQueue()
        }, 1L, 1L)
    }

    private fun stopDrainTaskIfIdle() {
        if (subscribers.isNotEmpty()) return
        drainTask?.cancel()
        drainTask = null
    }

    fun reloadFilters() {
        LogFilterManager.reload(subscribers)
    }

    fun getReadingPlayerUUIDs(): List<UUID> = readingUUIDs.toList()

    fun isReading(player: OfflinePlayer): Boolean = player.uniqueId in readingUUIDs

    fun subscribe(player: Player) {
        LogFilterManager.onPlayerSubscribe(player)
        subscribers.add(player)
        startDrainTaskIfNeeded()
    }

    fun unsubscribe(player: Player) {
        subscribers.remove(player)
        LogFilterManager.onPlayerUnsubscribe(player)
        stopDrainTaskIfIdle()
    }

    fun startReading(player: OfflinePlayer) {
        readingUUIDs.add(player.uniqueId)
        if (player.isOnline) {
            subscribe(player as Player)
        }
    }

    // Requires online player
    fun startReadingTemp(player: Player, seconds: Int) {
        subscribe(player)
        Bukkit.getScheduler().runTaskLater(ConsoleReader.instance, Runnable {
            unsubscribe(player)
        }, seconds * 20L)
    }

    fun stopReading(player: OfflinePlayer) {
        readingUUIDs.remove(player.uniqueId)
        if (player.isOnline) {
            unsubscribe(player as Player)
        }
    }

    fun stopReadingAll() {
        subscribers.toList().forEach { LogFilterManager.onPlayerUnsubscribe(it) }
        subscribers.clear()
        readingUUIDs.clear()
        drainTask?.cancel()
        drainTask = null
        logger.removeAppender(appender)
    }
}