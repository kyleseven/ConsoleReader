package me.kyleseven.consolereader

import co.aikar.commands.PaperCommandManager
import me.kyleseven.consolereader.commands.MainCommand
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.listeners.PlayerListener
import me.kyleseven.consolereader.logreader.LogAppenderManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

class ConsoleReader : JavaPlugin() {
    val isPaperMC: Boolean by lazy { checkServerType() }

    override fun onEnable() {
        instance = this
        checkServerType()
        loadConfigs()
        registerCommands()
        registerEvents()
        setupBStats()
        LogAppenderManager.setup(LogManager.getRootLogger() as Logger)
    }

    override fun onDisable() {
        LogAppenderManager.stopReadingAll()
    }

    private fun checkServerType(): Boolean {
        return try {
            Class.forName("com.destroystokyo.paper.PaperConfig")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
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

    private fun setupBStats() {
        Metrics(this, 9754)
    }

    companion object {
        lateinit var instance: ConsoleReader
            private set
    }
}