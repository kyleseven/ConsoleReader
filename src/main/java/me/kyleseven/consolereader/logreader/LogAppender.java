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
        String message = log.getMessage().getFormattedMessage();
        message = "[" + DateFormatUtils.format(log.getTimeMillis(), "HH:mm:ss") + " " + log.getLevel().toString() + "]: " + message;
        Utils.sendMsg(player, MainConfig.getInstance().getLogColor() + message);
    }
}
