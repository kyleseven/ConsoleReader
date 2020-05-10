package me.kyleseven.consolereader;

import co.aikar.commands.PaperCommandManager;
import me.kyleseven.consolereader.commands.MainCommand;
import me.kyleseven.consolereader.config.MainConfig;
import me.kyleseven.consolereader.listeners.PlayerListener;
import me.kyleseven.consolereader.logreader.LogAppenderManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConsoleReader extends JavaPlugin {

    private static ConsoleReader plugin;
    private static PaperCommandManager commandManager;

    @Override
    public void onEnable() {
        plugin = this;
        loadConfigs();
        registerCommands();
        registerEvents();
        LogAppenderManager.setup((Logger) LogManager.getRootLogger());
    }

    @Override
    public void onDisable() {
        LogAppenderManager.stopReadingAll();
    }

    public static ConsoleReader getPlugin() {
        return plugin;
    }

    private void loadConfigs() {
        MainConfig.getInstance();
    }

    private void registerCommands() {
        commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new MainCommand());
    }

    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }
}
