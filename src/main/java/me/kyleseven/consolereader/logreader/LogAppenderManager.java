package me.kyleseven.consolereader.logreader;

import org.apache.logging.log4j.core.Logger;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LogAppenderManager {
    private static HashMap<UUID, LogAppender> logAppenders;
    private static Logger logger;

    public static void setup(Logger logger) {
        LogAppenderManager.logAppenders = new HashMap<>();
        LogAppenderManager.logger = logger;
    }

    public static void startReading(Player player) {
        LogAppender appender = new LogAppender(player);
        logAppenders.put(player.getUniqueId(), appender);
        logger.addAppender(appender);
    }

    public static void stopReading(Player player) {
        logger.removeAppender(logAppenders.get(player.getUniqueId()));
        logAppenders.remove(player.getUniqueId());
    }

    public static void stopReadingAll() {
        for (LogAppender appender : logAppenders.values()) {
            logger.removeAppender(appender);
        }

        logAppenders.clear();
    }

    public static boolean isReading(Player player) {
        return logAppenders.containsKey(player.getUniqueId());
    }
}
