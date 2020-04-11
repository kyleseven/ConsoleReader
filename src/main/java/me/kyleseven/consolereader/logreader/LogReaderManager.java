package me.kyleseven.consolereader.logreader;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class LogReaderManager {
    private static HashMap<UUID, LogReaderThread> logReaderThreads;

    public static void setup() {
        logReaderThreads = new HashMap<>();
    }

    public static void startReading(Player player) {
        logReaderThreads.put(player.getUniqueId(), new LogReaderThread(player));
    }

    public static void stopReading(Player player) {
        logReaderThreads.get(player.getUniqueId()).stop();
        logReaderThreads.remove(player.getUniqueId());
    }

    public static void stopReadingAll() {
        for (LogReaderThread thread : logReaderThreads.values()) {
            thread.stop();
        }
    }

    public static boolean isReading(Player player) {
        return logReaderThreads.containsKey(player.getUniqueId());
    }
}
