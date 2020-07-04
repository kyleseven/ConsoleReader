package me.kyleseven.consolereader.config

import me.kyleseven.consolereader.ConsoleReader
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class ConfigLoader(private val fileName: String) {
    private val plugin = ConsoleReader.instance
    private var configFile = File(plugin.dataFolder, fileName)
    protected var config = loadConfig()

    protected fun loadConfig(): YamlConfiguration {
        if (!configFile.exists()) {
            plugin.logger.info("Creating $fileName file...")
            plugin.saveResource(fileName, false)
        } else {
            plugin.logger.info("Loading $fileName file...")
        }
        return YamlConfiguration.loadConfiguration(configFile)
    }
}