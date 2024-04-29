package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.ConsoleReader
import org.apache.logging.log4j.core.Logger
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

object LogAppenderManager {
    private lateinit var logger: Logger
    private lateinit var logAppenders: HashMap<UUID, LogAppender?>

    fun setup(logger: Logger) {
        this.logger = logger
        logAppenders = hashMapOf()
    }

    fun getReadingPlayerUUIDs(): List<UUID> {
        return logAppenders.keys.toList()
    }

    fun isReading(player: OfflinePlayer): Boolean {
        return logger.appenders.containsKey("ConsoleReader-${player.uniqueId}") || logAppenders.containsKey(player.uniqueId)
    }

    fun startReading(player: OfflinePlayer) {
        if (player.isOnline) {
            val appender = LogAppender(player as Player)
            logger.addAppender(appender)
            logAppenders[player.uniqueId] = appender
        } else {
            logAppenders[player.uniqueId] = null
        }
    }

    // Requires online player
    fun startReadingTemp(player: Player, seconds: Int) {
        val appender = LogAppender(player)
        logger.addAppender(appender)
        Bukkit.getScheduler().runTaskLater(ConsoleReader.instance, Runnable {
            logger.removeAppender(appender)
        }, seconds * 20L)
    }

    fun stopReading(player: OfflinePlayer) {
        if (logAppenders[player.uniqueId] != null) {
            logger.removeAppender(logAppenders[player.uniqueId])
        }
        logAppenders.remove(player.uniqueId)
    }

    // Requires online player
    fun stopReadingTemp(player: Player) {
        if (player.uniqueId in logAppenders.keys) {
            logger.removeAppender(logAppenders[player.uniqueId])
        }
    }

    fun stopReadingAll() {
        for (app in logAppenders.values) {
            if (app != null) {
                logger.removeAppender(app)
            }
        }

        logAppenders.clear()
    }
}