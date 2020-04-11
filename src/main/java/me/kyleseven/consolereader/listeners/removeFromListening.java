package me.kyleseven.consolereader.listeners;

import me.kyleseven.consolereader.commands.MainCommand;
import me.kyleseven.consolereader.logreader.LogReader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.UUID;

public class removeFromListening implements Listener {
    // Stop player from reading console once they leave

    @EventHandler
    public void removeListeningPlayer(PlayerQuitEvent e) {
        HashMap<UUID, LogReader> listeningPlayers = MainCommand.getListeningPlayers();
        listeningPlayers.get(e.getPlayer().getUniqueId()).stopReading();
        listeningPlayers.remove(e.getPlayer().getUniqueId());
    }
}
