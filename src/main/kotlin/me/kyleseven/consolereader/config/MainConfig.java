package me.kyleseven.consolereader.config;

import net.md_5.bungee.api.ChatColor;

import java.util.List;

public class MainConfig extends ConfigLoader {
    private static MainConfig mainConfig;

    public MainConfig() {
        super("config.yml");
    }

    public static MainConfig getInstance() {
        if (mainConfig == null) {
            mainConfig = new MainConfig();
        }
        return mainConfig;
    }

    public static void reload() {
        getInstance().loadFile();
    }

    /*
    Config keys
     */

    public String getVersion() {
        return config.getString("version");
    }

    public String getPrefix() {
        return config.getString("prefix");
    }

    public ChatColor getLogColor() {
        return ChatColor.valueOf(config.getString("log_color"));
    }

    public boolean getShowChat() {
        return config.getBoolean("show_chat");
    }

    public boolean getShowOwnCommands() {
        return config.getBoolean("show_own_commands");
    }

    public List<String> getForbiddenCommands() {
        return config.getStringList("forbidden_commands");
    }
}
