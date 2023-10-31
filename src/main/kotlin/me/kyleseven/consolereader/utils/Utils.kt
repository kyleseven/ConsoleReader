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

fun parseANSI(message: String): String {
    var messageToParse = message
    val ansiMap = mapOf(
        "\u001B[0;32;1m" to ChatColor.GREEN,
        "\u001B[0;36;1m" to ChatColor.AQUA,
        "\u001B[0;31;1m" to ChatColor.RED,
        "\u001B[0;35;1m" to ChatColor.LIGHT_PURPLE,
        "\u001B[0;33;1m" to ChatColor.YELLOW,
        "\u001B[0;37;1m" to ChatColor.WHITE,
        "\u001B[0;30;22m" to ChatColor.BLACK,
        "\u001B[0;34;22m" to ChatColor.DARK_BLUE,
        "\u001B[0;32;22m" to ChatColor.DARK_GREEN,
        "\u001B[0;36;22m" to ChatColor.DARK_AQUA,
        "\u001B[0;31;22m" to ChatColor.DARK_RED,
        "\u001B[0;35;22m" to ChatColor.DARK_PURPLE,
        "\u001B[0;33;22m" to ChatColor.GOLD,
        "\u001B[0;37;22m" to ChatColor.GRAY,
        "\u001B[0;30;1m" to ChatColor.DARK_GRAY,
        "\u001B[5m" to ChatColor.MAGIC,
        "\u001B[21m" to ChatColor.BOLD,
        "\u001B[9m" to ChatColor.STRIKETHROUGH,
        "\u001B[4m" to ChatColor.UNDERLINE,
        "\u001B[3m" to ChatColor.ITALIC,
        "\u001B[m" to ChatColor.RESET
    )

    ansiMap.forEach {
        messageToParse = messageToParse.replace(it.key, it.value.toString())
    }

    // TODO: Figure out how to handle ALL ANSI Escape sequences
    // Clean up remaining ANSI escape sequences
    val ansiCodeRegex = "\u001B\\[[0-9;]*m".toRegex()
    messageToParse = messageToParse.replace(ansiCodeRegex, "")

    return messageToParse
}