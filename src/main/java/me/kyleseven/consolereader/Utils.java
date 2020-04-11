package me.kyleseven.consolereader;

import me.kyleseven.consolereader.config.MainConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class Utils {
    public static void sendMsg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
    }

    public static void sendPrefixMsg(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', MainConfig.getInstance().getPrefix() + msg));
    }
}
