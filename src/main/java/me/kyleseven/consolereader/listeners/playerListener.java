package me.kyleseven.consolereader.listeners;

import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.logreader.LogAppenderManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class playerListener implements Listener {
    @EventHandler
    public void removeReadingPlayer(PlayerQuitEvent e) {
        // Remove the appender if player is offline
        if (LogAppenderManager.isReading(e.getPlayer())) {
            LogAppenderManager.stopReadingOffline(e.getPlayer());
        }
    }

    @EventHandler
    public void resumeReadingPlayer(PlayerJoinEvent e) {
        // Resume reading if the player comes back online
        if (LogAppenderManager.isReading(e.getPlayer())) {
            Utils.sendPrefixMsg(e.getPlayer(), "Console reading resumed!");
            LogAppenderManager.startReading(e.getPlayer());
        }
    }
}
