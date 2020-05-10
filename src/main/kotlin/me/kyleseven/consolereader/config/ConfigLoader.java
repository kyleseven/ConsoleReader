package me.kyleseven.consolereader.config;

import me.kyleseven.consolereader.ConsoleReader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class ConfigLoader {
    protected ConsoleReader plugin = ConsoleReader.getPlugin();
    protected String fileName;
    protected File configFile;
    protected FileConfiguration config;

    public ConfigLoader(String relativePath, String fileName) {
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), relativePath + File.separator + fileName);
        loadFile();
    }

    public ConfigLoader(String fileName) {
        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);
        loadFile();
    }

    protected void loadFile() {
        if (!configFile.exists()) {
            plugin.getLogger().info("Creating " + fileName + " file...");
            plugin.saveResource(fileName, false);
        } else {
            plugin.getLogger().info("Loading " + fileName + " file...");
        }

        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
