package me.kyleseven.consolereader

import co.aikar.commands.PaperCommandManager
import me.kyleseven.consolereader.commands.MainCommand
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.listeners.PlayerListener
import me.kyleseven.consolereader.logappender.LogAppenderManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.bukkit.plugin.java.JavaPlugin

class ConsoleReader : JavaPlugin() {
    override fun onEnable() {
        instance = this
        loadConfigs()
        registerCommands()
        registerEvents()
        LogAppenderManager.setup(LogManager.getRootLogger() as Logger)
    }

    override fun onDisable() {
        LogAppenderManager.stopReadingAll()
    }

    private fun loadConfigs() {
        MainConfig
    }

    private fun registerCommands() {
        PaperCommandManager(this).registerCommand(MainCommand())
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(PlayerListener(), this)
    }

    companion object {
        var instance: ConsoleReader? = null
            private set
    }
}