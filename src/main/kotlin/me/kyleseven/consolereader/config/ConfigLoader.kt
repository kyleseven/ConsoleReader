package me.kyleseven.consolereader.config

import co.aikar.commands.annotation.Dependency
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

abstract class ConfigLoader {
    @Dependency
    private lateinit var plugin: Plugin
    private var fileName: String
    private var configFile: File
    protected var config: FileConfiguration? = null

    constructor(relativePath: String, fileName: String) {
        this.fileName = fileName
        configFile = File(plugin.dataFolder, relativePath + File.separator + fileName)
        loadFile()
    }

    constructor(fileName: String) {
        this.fileName = fileName
        configFile = File(plugin.dataFolder, fileName)
        loadFile()
    }

    protected fun loadFile() {
        if (!configFile.exists()) {
            plugin.logger.info("Creating $fileName file...")
            plugin.saveResource(fileName, false)
        } else {
            plugin.logger.info("Loading $fileName file...")
        }
        config = YamlConfiguration.loadConfiguration(configFile)
    }
}