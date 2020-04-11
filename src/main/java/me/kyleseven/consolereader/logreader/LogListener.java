package me.kyleseven.consolereader.logreader;

import org.bukkit.entity.Player;

public class LogListener {
    Player player;
    LogListenThread playerThread;

    public LogListener(Player player) {
        this.player = player;
    }

    public void startReading() {
        playerThread = new LogListenThread(player);
    }

    public void stopReading() {
        playerThread.stop();
    }
}
