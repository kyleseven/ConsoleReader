package me.kyleseven.consolereader.logreader;

import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.config.MainConfig;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogAppender extends AbstractAppender {

    private final Player player;
    private final SimpleDateFormat dateFormatter;

    public LogAppender(Player player) {
        super("LogReader", null, null, false, null);
        this.player = player;
        dateFormatter = new SimpleDateFormat("HH:mm:ss");
        start();
    }

    @Override
    public void append(LogEvent event) {
        LogEvent log = event.toImmutable();
        String message = log.getMessage().getFormattedMessage();
        message = "[" + dateFormatter.format(new Date(log.getTimeMillis()) + log.getLevel().toString() + "] " + message);
        Utils.sendMsg(player, MainConfig.getInstance().getLogColor() + message);
    }
}
