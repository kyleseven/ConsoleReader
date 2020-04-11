package me.kyleseven.consolereader.logreader;

import org.bukkit.entity.Player;

public class LogReader {
    Player player;
    LogReaderThread playerThread;

    public LogReader(Player player) {
        this.player = player;
    }

    public void startReading() {
        playerThread = new LogReaderThread(player);
    }

    public void stopReading() {
        playerThread.stop();
    }
}
