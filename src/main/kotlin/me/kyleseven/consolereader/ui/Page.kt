package me.kyleseven.consolereader.ui

import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.sendColorMsg
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.CommandSender

data class Page(
    val title: String,
    val content: List<String>,
    val pageNumber: Int,
    val maxPageNumber: Int,
    val prevCmd: String,
    val nextCmd: String
)

fun CommandSender.sendPage(page: Page) {
    val finalComponent = ComponentBuilder("")

    if (page.content.isEmpty()) {
        finalComponent.append("Error: Invalid Page Number. Valid Range: 1-${page.maxPageNumber}").color(ChatColor.RED)
        spigot().sendMessage(*finalComponent.create())
        return
    }

    val header = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
        .append(page.title).color(ChatColor.DARK_AQUA)
        .append(" ======------").color(ChatColor.DARK_GRAY)

    val footer = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
        .append("${page.pageNumber} of ${page.maxPageNumber}").color(ChatColor.AQUA)
        .append(" ======------").color(ChatColor.DARK_GRAY)

    spigot().sendMessage(*header.create())
    for (item in page.content) {
        sendColorMsg("${MainConfig.logColor}$item")
    }
    spigot().sendMessage(*footer.create())
}