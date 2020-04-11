package me.kyleseven.consolereader.logreader;

import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogReaderThread implements Runnable {
    private boolean exit;
    private final Player player;
    Thread t;

    LogReaderThread(Player player) {
        this.player = player;
        this.exit = false;
        t = new Thread(this);
        t.start();
    }

    @Override
    public void run() {
        String logPath = Bukkit.getServer().getWorldContainer().toString() + File.separator + "logs" + File.separator + "latest.log";
        File logFile = new File(logPath);
        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String line;
            br.skip(logFile.length());
            while (!exit) {
                line = br.readLine();
                if (line == null) {
                    Thread.sleep(500);
                }
                else {
                    Utils.sendMsg(player, MainConfig.getInstance().getLogColor() + line);
                }
            }
        }
        catch (IOException | InterruptedException e) {
            Utils.sendPrefixMsg(player, "&4Exception: " + e.getMessage());
        }
    }

    public void stop() {
        exit = true;
    }
}
