package me.kyleseven.consolereader.logreader;

import me.kyleseven.consolereader.config.MainConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

        // Log components
        ChatColor logColor = MainConfig.getInstance().getLogColor();
        String logMessage = logColor + log.getMessage().getFormattedMessage();
        String logDate = DateFormatUtils.format(log.getTimeMillis(), "yyyy-MM-dd");
        String logTime = DateFormatUtils.format(log.getTimeMillis(), "HH:mm:ss");
        String logLevel = log.getLevel().toString();
        String loggerName = log.getLoggerName();
        String threadName = log.getThreadName();
        String messagePrefix = logColor + "[" + logTime + " " + logLevel + "]: ";

        /*
        Filtering console messages here.
        - Enable showing chat or not.
        - Show own commands in chat or not.
        - Showing logger name if it is not from the game itself.
        - Adding color to WARN, FATAL, and ERROR messages.
         */

        if (!MainConfig.getInstance().getShowChat()) {
            if (threadName.contains("Async Chat Thread")) {
                return;
            }
        }

        if (!MainConfig.getInstance().getShowOwnCommands()) {
            if (logMessage.contains(player.getName() + " issued server command") && loggerName.contains("net.minecraft.server")) {
                return;
            }
        }

        if (loggerName != null && !(loggerName.contains("net.minecraft") || loggerName.equals("Minecraft") || loggerName.isEmpty())) {
            messagePrefix += "[" + loggerName + "] ";
        }

        if (logLevel.equals("WARN")) {
            messagePrefix = ChatColor.YELLOW + messagePrefix;
        } else if (logLevel.equals("FATAL") || logLevel.equals("ERROR")) {
            messagePrefix = ChatColor.RED + messagePrefix;
        }

        // Creating Hover Text
        TextComponent chatLogPrefix = new TextComponent(TextComponent.fromLegacyText(messagePrefix));
        TextComponent chatLogMessage = new TextComponent(TextComponent.fromLegacyText(logMessage));
        ComponentBuilder hoverText = new ComponentBuilder()
                .append("Time: ").color(ChatColor.GRAY).append(logDate + " " + logTime + "\n").color(ChatColor.WHITE)
                .append("Log Level: ").color(ChatColor.GRAY).append(logLevel + "\n").color(ChatColor.WHITE)
                .append("Logger: ").color(ChatColor.GRAY).append(((loggerName == null || loggerName.isEmpty()) ? "None" : loggerName) + "\n").color(ChatColor.WHITE)
                .append("Thread: ").color(ChatColor.GRAY).append(threadName).color(ChatColor.WHITE);
        chatLogPrefix.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.create()));

        player.spigot().sendMessage(chatLogPrefix, chatLogMessage);
    }
}
