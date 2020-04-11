package me.kyleseven.consolereader.config;

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
        mainConfig = null;
        getInstance();
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

    public String getLogColor() {
        return "&" + config.getString("log_color");
    }

    public int getRefreshTime() {
        return config.getInt("refresh_time");
    }
}
