package me.kyleseven.consolereader.logreader;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

public class LogAppender extends AbstractAppender {

    public LogAppender() {
        super("LogReader", null, null, false);
        start();
    }

    @Override
    public void append(LogEvent event) {
        LogEvent log = event.toImmutable();
        String message = log.getMessage().getFormattedMessage();
    }
}
