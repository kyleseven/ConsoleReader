package me.kyleseven.consolereader.config

import me.kyleseven.consolereader.ConsoleReader
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class ConfigLoader {
    private var plugin: ConsoleReader? = ConsoleReader.instance
    private var fileName: String
    private var configFile: File
    protected var config: FileConfiguration? = null

    protected constructor(relativePath: String, fileName: String) {
        this.fileName = fileName
        configFile = File(plugin!!.dataFolder, relativePath + File.separator + fileName)
        loadFile()
    }

    protected constructor(fileName: String) {
        this.fileName = fileName
        configFile = File(plugin!!.dataFolder, fileName)
        loadFile()
    }

    protected fun loadFile() {
        if (!configFile.exists()) {
            plugin!!.logger.info("Creating $fileName file...")
            plugin!!.saveResource(fileName, false)
        } else {
            plugin!!.logger.info("Loading $fileName file...")
        }
        config = YamlConfiguration.loadConfiguration(configFile)
    }
}