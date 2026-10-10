package me.kyleseven.consolereader.utils

import me.kyleseven.consolereader.config.MainConfig
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender

fun CommandSender.sendColorMsg(message: String) {
    spigot().sendMessage(TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', message)))
}

fun CommandSender.sendPrefixMsg(message: String) {
    sendColorMsg(MainConfig.prefix + message)
}

fun CommandSender.sendError(message: String) {
    val prefix = TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', MainConfig.prefix))
    val error = TextComponent(message)
    error.color = ChatColor.RED

    spigot().sendMessage(prefix, error)
}
