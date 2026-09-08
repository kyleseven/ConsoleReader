package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.Logger
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue

object LogAppenderManager {
    private const val QUEUE_CAPACITY = 1000
    private const val MAX_EVENTS_PER_TICK = 100

    private lateinit var logger: Logger
    // Subscriber collections and delivery are confined to the server thread.
    private val persistent = mutableSetOf<UUID>()
    private val temporary = mutableMapOf<UUID, BukkitTask>()
    private val onlineReaders = mutableMapOf<UUID, Player>()
    private var recipientSnapshot: List<Player> = emptyList()
    private val queue = ArrayBlockingQueue<LogEvent>(QUEUE_CAPACITY)
    private val queueLock = Any()
    private var appender: LogAppender? = null
    private var drainTask: BukkitTask? = null

    @Volatile private var active = false
    @Volatile private var hasSubscribers = false

    fun setup(logger: Logger) {
        stopReadingAll()
        this.logger = logger
        appender = LogAppender().also {
            it.start()
            logger.addAppender(it)
        }
        active = true
    }

    fun getReadingPlayerUUIDs(): List<UUID> {
        return persistent.toList()
    }

    fun isReading(player: OfflinePlayer): Boolean {
        return isReadingPersistent(player) || player.uniqueId in temporary
    }

    fun isReadingPersistent(player: OfflinePlayer): Boolean {
        return player.uniqueId in persistent
    }

    // Keeps the preference across reconnects and replaces any temporary subscription.
    fun startReading(player: OfflinePlayer) {
        persistent.add(player.uniqueId)
        temporary.remove(player.uniqueId)?.cancel()
        val onlinePlayer = player.player
        if (onlinePlayer != null && onlinePlayer.isOnline) {
            onlineReaders[player.uniqueId] = onlinePlayer
        }
        syncSubscriberState()
    }

    // Refreshes the expiry timer without affecting persistent readers.
    fun startReadingTemp(player: Player, seconds: Int) {
        if (isReadingPersistent(player)) {
            return
        }
        val uuid = player.uniqueId
        temporary.remove(uuid)?.cancel()
        temporary[uuid] = Bukkit.getScheduler().runTaskLater(ConsoleReader.instance, Runnable {
            temporary.remove(uuid)
            if (uuid !in persistent) {
                onlineReaders.remove(uuid)
                appender?.clearFilterCache(uuid)
            }
            syncSubscriberState()
        }, seconds * 20L)
        if (player.isOnline) {
            onlineReaders[uuid] = player
        }
        syncSubscriberState()
    }

    fun stopReading(player: OfflinePlayer) {
        persistent.remove(player.uniqueId)
        stopReadingTemp(player)
    }

    fun stopReadingTemp(player: OfflinePlayer) {
        temporary.remove(player.uniqueId)?.cancel()
        if (!isReadingPersistent(player)) {
            onlineReaders.remove(player.uniqueId)
            appender?.clearFilterCache(player.uniqueId)
        }
        syncSubscriberState()
    }

    // Releases online state while preserving the persistent reading preference.
    fun playerQuit(player: Player) {
        temporary.remove(player.uniqueId)?.cancel()
        onlineReaders.remove(player.uniqueId)
        appender?.clearFilterCache(player.uniqueId)
        syncSubscriberState()
    }

    // Copies accepted events from logging threads for later server-thread delivery.
    internal fun enqueue(event: LogEvent) {
        if (!active || !hasSubscribers) {
            return
        }
        synchronized(queueLock) {
            if (!active || !hasSubscribers || queue.remainingCapacity() == 0) {
                return
            }
            // Drop the newest event when full; never log overflow through this appender.
            queue.offer(event.toImmutable())
        }
    }

    // Delivers a bounded batch using one stable recipient snapshot.
    private fun drain() {
        var event = queue.poll() ?: return
        val players = recipientSnapshot
        val currentAppender = appender ?: return
        for (index in 0 until MAX_EVENTS_PER_TICK) {
            currentAppender.deliver(event, players)
            if (index < MAX_EVENTS_PER_TICK - 1) {
                event = queue.poll() ?: return
            }
        }
    }

    // Blocks new events before releasing queued logs, tasks, caches, and subscriptions.
    fun stopReadingAll() {
        synchronized(queueLock) {
            active = false
            hasSubscribers = false
            queue.clear()
        }
        drainTask?.cancel()
        drainTask = null
        temporary.values.forEach { it.cancel() }
        temporary.clear()
        persistent.clear()
        onlineReaders.clear()
        recipientSnapshot = emptyList()
        appender?.let {
            it.clearFilterCache()
            logger.removeAppender(it)
            it.stop()
        }
        appender = null
    }

    // Discards player substitutions so the next delivery uses the reloaded filters.
    fun invalidateConfigCaches() {
        appender?.clearFilterCache()
    }

    // Aligns recipients, queue admission, and drain scheduling with online readership.
    private fun syncSubscriberState() {
        synchronized(queueLock) {
            hasSubscribers = onlineReaders.isNotEmpty()
            recipientSnapshot = onlineReaders.values.toList()
            if (!hasSubscribers) {
                queue.clear()
            }
        }
        if (active && hasSubscribers) {
            if (drainTask == null) {
                drainTask = Bukkit.getScheduler().runTaskTimer(ConsoleReader.instance, Runnable {
                    drain()
                }, 1L, 1L)
            }
        } else {
            drainTask?.cancel()
            drainTask = null
        }
    }
}
