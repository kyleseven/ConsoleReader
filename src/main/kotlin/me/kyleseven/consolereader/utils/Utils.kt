package me.kyleseven.consolereader.utils

import me.kyleseven.consolereader.config.MainConfig
import net.md_5.bungee.api.ChatColor
import org.bukkit.command.CommandSender

fun CommandSender.sendColorMsg(message: String) {
    sendMessage(ChatColor.translateAlternateColorCodes('&', message))
}

fun CommandSender.sendPrefixMsg(message: String) {
    sendMessage(ChatColor.translateAlternateColorCodes('&', MainConfig.prefix + message))
}
