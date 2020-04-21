package me.kyleseven.consolereader.logreader;

import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.config.MainConfig;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.entity.Player;

public class LogAppender extends AbstractAppender {

    private final Player player;

    public LogAppender(Player player) {
        super("LogReader", null, null, false);
        this.player = player;
        start();
    }

    @Override
    public void append(LogEvent event) {
        LogEvent log = event.toImmutable();
        String logMessage = log.getMessage().getFormattedMessage();
        String logLevel = log.getLevel().toString();
        String loggerName = log.getLoggerName();
        String completeMessage = "[" + DateFormatUtils.format(log.getTimeMillis(), "HH:mm:ss") + " " + logLevel + "]: ";
        if (loggerName != null && !(loggerName.contains("net.minecraft") || loggerName.equals("Minecraft") || loggerName.isEmpty())) {
            completeMessage += "[" + loggerName + "] ";
        }
        completeMessage += logMessage;
        if (logLevel.equals("WARN")) {
            completeMessage = "&e" + completeMessage;
        }
        else if (logLevel.equals("FATAL") || logLevel.equals("ERROR")) {
            completeMessage = "&c" + completeMessage;
        }
        Utils.sendMsg(player, completeMessage);
    }
}
