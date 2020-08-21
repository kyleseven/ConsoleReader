package me.kyleseven.consolereader

import co.aikar.commands.PaperCommandManager
import me.kyleseven.consolereader.commands.MainCommand
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.listeners.PlayerListener
import me.kyleseven.consolereader.logappender.LogAppenderManager
import me.kyleseven.consolereader.logview.LogFileManager
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.bukkit.plugin.java.JavaPlugin

class ConsoleReader : JavaPlugin() {
    var isPaperMC: Boolean = false

    override fun onEnable() {
        instance = this
        commandManager = PaperCommandManager(this)
        LogAppenderManager.setup(LogManager.getRootLogger() as Logger)
        LogFileManager.setup()
        checkServerType()
        loadConfigs()
        registerCommands()
        registerCompletions()
        registerEvents()
    }

    override fun onDisable() {
        LogAppenderManager.stopReadingAll()
        LogFileManager.cleanUp()
    }

    private fun checkServerType() {
        isPaperMC = try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager\$VersionData")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    private fun loadConfigs() {
        MainConfig
    }

    private fun registerCommands() {
        commandManager.registerCommand(MainCommand())
    }

    private fun registerCompletions() {
        commandManager.commandCompletions.registerAsyncCompletion("logs") {
            LogFileManager.logList.toList()
        }
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(PlayerListener(), this)
    }

    companion object {
        lateinit var instance: ConsoleReader
            private set

        private lateinit var commandManager: PaperCommandManager
    }
}