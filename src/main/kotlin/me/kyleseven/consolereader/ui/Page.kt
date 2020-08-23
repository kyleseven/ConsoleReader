package me.kyleseven.consolereader.ui

import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.sendColorMsg
import me.kyleseven.consolereader.utils.sendError
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
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
    if (page.content.isEmpty()) {
        sendError("Invalid Page Number. Valid Range: 1-${page.maxPageNumber}")
        return
    }

    val header = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
        .append(page.title).color(ChatColor.DARK_AQUA)
        .append(" ======------").color(ChatColor.DARK_GRAY)

    val footer = ComponentBuilder("------====").color(ChatColor.DARK_GRAY)

    if (page.pageNumber == 1) {
        footer.append(" « ").color(ChatColor.DARK_GRAY)
    } else {
        footer.append(" « ").color(ChatColor.GRAY)
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, page.prevCmd))
            .event(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ComponentBuilder("Previous Page").color(ChatColor.GRAY).create()
                )
            )
    }

    footer.append("${page.pageNumber} of ${page.maxPageNumber}", ComponentBuilder.FormatRetention.NONE)
        .color(ChatColor.AQUA)

    if (page.pageNumber == page.maxPageNumber) {
        footer.append(" » ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.DARK_GRAY)
    } else {
        footer.append(" » ", ComponentBuilder.FormatRetention.NONE).color(ChatColor.GRAY)
            .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, page.nextCmd))
            .event(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ComponentBuilder("Next Page").color(ChatColor.GRAY).create()
                )
            )
    }

    footer.append("====------", ComponentBuilder.FormatRetention.NONE).color(ChatColor.DARK_GRAY)

    spigot().sendMessage(*header.create())
    for (item in page.content) {
        sendColorMsg("${MainConfig.logColor}$item")
    }
    spigot().sendMessage(*footer.create())
}