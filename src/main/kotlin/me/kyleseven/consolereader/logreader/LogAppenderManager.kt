package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.utils.sendPrefixMsg
import org.apache.logging.log4j.core.Logger
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*

/*
One LogAppender is attached to the logger and queues every log line.
While at least one player is reading, a task takes lines from the queue every tick, formats each line once,
and sends it to every reader.

Everything here must be called from the main thread.
 */
object LogAppenderManager {
    // Lines sent per tick. Anything more waits for the next tick.
    private const val MAX_LINES_PER_TICK = 50

    private lateinit var logger: Logger
    private lateinit var logAppender: LogAppender

    // Players with console reading enabled, online or not
    private val readingPlayers: LinkedHashSet<UUID> = linkedSetOf()

    // Online players that are sent log lines, including temporary readers
    private val logReaders: HashMap<UUID, LogReader> = hashMapOf()

    private var sendTask: BukkitTask? = null

    fun setup(logger: Logger) {
        this.logger = logger
        logAppender = LogAppender()
        logger.addAppender(logAppender)
    }

    fun getReadingPlayerUUIDs(): List<UUID> {
        return readingPlayers.toList()
    }

    fun isReading(player: OfflinePlayer): Boolean {
        return player.uniqueId in readingPlayers
    }

    // Offline players will start reading when they join
    fun startReading(player: OfflinePlayer) {
        readingPlayers.add(player.uniqueId)

        val onlinePlayer = player.player
        if (onlinePlayer != null) {
            addLogReader(onlinePlayer).expiresAt = null
        }
    }

    // Requires online player
    fun startReadingTemp(player: Player, seconds: Int) {
        val logReader = logReaders[player.uniqueId]
        if (logReader != null && logReader.expiresAt == null) {
            return
        }

        addLogReader(player).expiresAt = System.currentTimeMillis() + seconds * 1000L
    }

    fun stopReading(player: OfflinePlayer) {
        readingPlayers.remove(player.uniqueId)
        removeLogReader(player.uniqueId)
    }

    // Stops sending log lines to the player, but reading stays enabled for when they join again
    fun stopReadingTemp(player: Player) {
        removeLogReader(player.uniqueId)
    }

    fun stopReadingAll() {
        readingPlayers.clear()
        logReaders.clear()
        updateSendTask()

        logger.removeAppender(logAppender)
        logAppender.stop()
    }

    // Needed after a config reload
    fun reloadFilters() {
        for (logReader in logReaders.values) {
            logReader.compileFilters()
        }
    }

    private fun addLogReader(player: Player): LogReader {
        val logReader = logReaders.getOrPut(player.uniqueId) { LogReader(player) }
        updateSendTask()
        return logReader
    }

    private fun removeLogReader(uuid: UUID) {
        logReaders.remove(uuid)
        updateSendTask()
    }

    // The task only runs while there are readers
    private fun updateSendTask() {
        if (logReaders.isNotEmpty() && sendTask == null) {
            logAppender.accepting = true
            sendTask = Bukkit.getScheduler().runTaskTimer(ConsoleReader.instance, Runnable { sendQueuedLines() }, 1L, 1L)
        } else if (logReaders.isEmpty() && sendTask != null) {
            logAppender.accepting = false
            sendTask?.cancel()
            sendTask = null
            logAppender.clear()
        }
    }

    /*
    Do not log anything from here.
    It would be queued by the appender and sent back to the readers.
     */
    private fun sendQueuedLines() {
        if (logReaders.values.removeIf { it.isExpired() }) {
            updateSendTask()
        }

        val droppedCount = logAppender.takeDroppedCount()
        if (droppedCount > 0) {
            for (logReader in logReaders.values) {
                logReader.player.sendPrefixMsg("Skipped $droppedCount lines of console output.")
            }
        }

        for (i in 0 until MAX_LINES_PER_TICK) {
            val line = logAppender.poll() ?: return
            val formattedLine = LogFormatter.format(line)

            for (logReader in logReaders.values) {
                if (!logReader.isFiltered(formattedLine.plainMessage)) {
                    logReader.player.spigot().sendMessage(formattedLine.component)
                }
            }
        }
    }
}
