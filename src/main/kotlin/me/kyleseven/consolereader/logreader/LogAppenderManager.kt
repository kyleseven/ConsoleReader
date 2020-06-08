package me.kyleseven.consolereader.logreader

import org.apache.logging.log4j.core.Logger
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.thread

object LogAppenderManager {
    private lateinit var logger: Logger
    private lateinit var logAppenders: HashMap<UUID, LogAppender>

    fun setup(logger: Logger) {
        this.logger = logger
        logAppenders = hashMapOf()
    }

    fun getReadingPlayerUUIDs(): List<UUID> {
        return logAppenders.keys.toList()
    }

    fun isReading(player: Player): Boolean {
        return logger.appenders.containsKey("ConsoleReader-${player.uniqueId}") || logAppenders.containsKey(player.uniqueId)
    }

    fun startReading(player: Player) {
        val appender = LogAppender(player)
        logAppenders[player.uniqueId] = appender
        logger.addAppender(appender)
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
        logger.removeAppender(logAppenders[player.uniqueId])
        logAppenders.remove(player.uniqueId)
    }

    fun stopReadingTemp(player: Player) {
        logger.removeAppender(logAppenders[player.uniqueId])
    }

    fun stopReadingAll() {
        for (app in logAppenders.values) {
            logger.removeAppender(app)
        }

        logAppenders.clear()
    }
}