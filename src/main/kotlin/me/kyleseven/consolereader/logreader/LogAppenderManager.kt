package me.kyleseven.consolereader.logreader

import org.apache.logging.log4j.core.Logger
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

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

    /*
    The following functions are overloaded to properly handle both online and offline players:
        - isReading()
        - startReading()
        - stopReading()

     The difference between temp functions and their normal counterparts is that the temp functions
     (startReadingTemp() and stopReadingTemp()) do not add/remove players to/from the logAppenders HashMap.
     */

    fun isReading(player: Player): Boolean {
        return logger.appenders.containsKey("ConsoleReader-${player.uniqueId}") || logAppenders.containsKey(player.uniqueId)
    }

    fun isReading(player: OfflinePlayer): Boolean {
        return logAppenders.containsKey(player.uniqueId)
    }

    fun startReading(player: Player) {
        val appender = LogAppender(player)
        logAppenders[player.uniqueId] = appender
        logger.addAppender(appender)
    }

    fun startReading(player: OfflinePlayer) {
        logAppenders[player.uniqueId] = null
    }

    fun startReadingTemp(player: Player, milliseconds: Long) {
        val appender = LogAppender(player)
        logger.addAppender(appender)
        thread {
            Thread.sleep(milliseconds)
            logger.removeAppender(appender)
        }
    }

    fun stopReading(player: Player) {
        if (logAppenders[player.uniqueId] != null) {
            logger.removeAppender(logAppenders[player.uniqueId])
        }
        logAppenders.remove(player.uniqueId)
    }

    fun stopReading(player: OfflinePlayer) {
        logAppenders.remove(player.uniqueId)
    }

    fun stopReadingTemp(player: Player) {
        logger.removeAppender(logAppenders[player.uniqueId])
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