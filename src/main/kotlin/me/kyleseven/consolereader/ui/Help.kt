package me.kyleseven.consolereader.ui

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.command.CommandSender

data class Command(
    val name: String = "/cr",
    val args: String = "",
    val aliases: ArrayList<String> = arrayListOf(),
    val description: String = "ConsoleReader command."
)

fun CommandSender.sendHelpMenu(vararg commands: Command) {
    val header = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
        .append("ConsoleReader Help").color(ChatColor.DARK_AQUA)
        .append(" ======------").color(ChatColor.DARK_GRAY)

    spigot().sendMessage(*header.create())

    for (command in commands) {
        val helpEntry = ComponentBuilder("")
        val hoverText = ComponentBuilder("")

        if (command.aliases.isNotEmpty()) {
            hoverText.append("Aliases: ").color(ChatColor.GRAY)
        }

        for ((i, alias) in command.aliases.withIndex()) {
            hoverText.append(alias.trim()).color(ChatColor.DARK_AQUA)
            if (i != command.aliases.lastIndex) {
                hoverText.append(", ").color(ChatColor.GRAY)
            } else if (command.aliases[i].isNotEmpty()) {
                hoverText.append("\n")
            }
        }

        // Add ClickEvent and HoverEvent based on command arguments
        if (command.args.isEmpty() || command.args.matches(Regex("\\[(.*?)]"))) {
            helpEntry.append(command.name).color(ChatColor.DARK_AQUA)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, command.name))
                .event(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        hoverText.append("Click to run.").color(ChatColor.GRAY).create()
                    )
                )
        } else {
            helpEntry.append(command.name).color(ChatColor.DARK_AQUA)
                .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.name))
                .event(
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        hoverText.append("Click to suggest.").color(ChatColor.GRAY).create()
                    )
                )
        }

        helpEntry.append(" ${command.args}".ifBlank { "" }, ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA)
            .append(" - ").color(ChatColor.DARK_GRAY)
            .append(command.description).color(ChatColor.GRAY)

        spigot().sendMessage(*helpEntry.create())
    }
}