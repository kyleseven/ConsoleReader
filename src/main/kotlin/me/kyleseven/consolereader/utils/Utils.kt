package me.kyleseven.consolereader.utils

import me.kyleseven.consolereader.config.MainConfig
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

fun CommandSender.sendColorMsg(message: String) {
    sendMessage(ChatColor.translateAlternateColorCodes('&', message))
}

fun CommandSender.sendPrefixMsg(message: String) {
    sendMessage(ChatColor.translateAlternateColorCodes('&', MainConfig.prefix + message))
}

fun parseANSI(message: String): String {
    var messageToParse = message
    val ansiMap = mapOf(
        "\u001B[0;32;1m" to net.md_5.bungee.api.ChatColor.GREEN,
        "\u001B[0;36;1m" to net.md_5.bungee.api.ChatColor.AQUA,
        "\u001B[0;31;1m" to net.md_5.bungee.api.ChatColor.RED,
        "\u001B[0;35;1m" to net.md_5.bungee.api.ChatColor.LIGHT_PURPLE,
        "\u001B[0;33;1m" to net.md_5.bungee.api.ChatColor.YELLOW,
        "\u001B[0;37;1m" to net.md_5.bungee.api.ChatColor.WHITE,
        "\u001B[0;30;22m" to net.md_5.bungee.api.ChatColor.BLACK,
        "\u001B[0;34;22m" to net.md_5.bungee.api.ChatColor.DARK_BLUE,
        "\u001B[0;32;22m" to net.md_5.bungee.api.ChatColor.DARK_GREEN,
        "\u001B[0;36;22m" to net.md_5.bungee.api.ChatColor.DARK_AQUA,
        "\u001B[0;31;22m" to net.md_5.bungee.api.ChatColor.DARK_RED,
        "\u001B[0;35;22m" to net.md_5.bungee.api.ChatColor.DARK_PURPLE,
        "\u001B[0;33;22m" to net.md_5.bungee.api.ChatColor.GOLD,
        "\u001B[0;37;22m" to net.md_5.bungee.api.ChatColor.GRAY,
        "\u001B[0;30;1m" to net.md_5.bungee.api.ChatColor.DARK_GRAY,
        "\u001B[5m" to net.md_5.bungee.api.ChatColor.MAGIC,
        "\u001B[21m" to net.md_5.bungee.api.ChatColor.BOLD,
        "\u001B[9m" to net.md_5.bungee.api.ChatColor.STRIKETHROUGH,
        "\u001B[4m" to net.md_5.bungee.api.ChatColor.UNDERLINE,
        "\u001B[3m" to net.md_5.bungee.api.ChatColor.ITALIC,
        "\u001B[m" to net.md_5.bungee.api.ChatColor.RESET
    )

    for ((ansi, color) in ansiMap) {
        if (messageToParse.contains(ansi)) {
            messageToParse = messageToParse.replace(ansi, color.toString())
        }
    }

    return messageToParse
}