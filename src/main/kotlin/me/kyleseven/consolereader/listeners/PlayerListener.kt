package me.kyleseven.consolereader.listeners

import me.kyleseven.consolereader.logreader.LogAppenderManager
import me.kyleseven.consolereader.utils.sendPrefixMsg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {
    @EventHandler
    fun removeReadingPlayer(e: PlayerQuitEvent) {
        LogAppenderManager.unsubscribe(e.player)
    }

    @EventHandler
    fun resumeReadingPlayer(e: PlayerJoinEvent) {
        if (LogAppenderManager.isReading(e.player)) {
            if (e.player.hasPermission("consolereader.read")) {
                e.player.sendPrefixMsg("Console reading resumed!")
                LogAppenderManager.startReading(e.player)
            } else {
                LogAppenderManager.stopReading(e.player)
            }
        }
    }
}