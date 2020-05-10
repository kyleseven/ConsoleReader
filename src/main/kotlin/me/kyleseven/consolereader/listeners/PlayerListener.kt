package me.kyleseven.consolereader.listeners

import me.kyleseven.consolereader.logreader.LogAppenderManager
import me.kyleseven.consolereader.sendPrefixMsg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {
    @EventHandler
    fun removeReadingPlayer(e: PlayerQuitEvent) {
        // Remove the appender if player is offline
        if (LogAppenderManager.isReading(e.player)) {
            LogAppenderManager.stopReadingTemp(e.player)
        }
    }

    @EventHandler
    fun resumeReadingPlayer(e: PlayerJoinEvent) {
        if (LogAppenderManager.isReading(e.player)) {
            e.player.sendPrefixMsg("Console reading resumed!")
            LogAppenderManager.startReading(e.player)
        }
    }
}