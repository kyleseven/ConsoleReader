package me.kyleseven.consolereader.listeners;

import me.kyleseven.consolereader.logreader.LogAppenderManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class playerListener implements Listener {
    // Stop player from reading console once they leave

    @EventHandler
    public void removeReadingPlayer(PlayerQuitEvent e) {
        if (LogAppenderManager.isReading(e.getPlayer())) {
            LogAppenderManager.stopReading(e.getPlayer());
        }
    }
}
