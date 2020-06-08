package me.kyleseven.consolereader.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.logreader.LogAppenderManager
import me.kyleseven.consolereader.utils.sendPrefixMsg
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("cr|consolereader")
@Description("Use the ConsoleReader plugin.")
class MainCommand : BaseCommand() {
    @CatchUnknown
    fun onInvalid(sender: CommandSender) {
        sender.sendPrefixMsg("&cUnknown subcommand.")
    }

    @Subcommand("help|h")
    @CommandPermission("consolereader.read")
    @HelpCommand
    @Default
    fun onHelp(sender: CommandSender) {
        // Each array in the array is [command, arguments, aliases, description]
        val commands = arrayOf(arrayOf("/cr help", "", "/cr, /cr h", "Shows this help menu."),
            arrayOf("/cr read", "[player]", "/cr r", "Toggle console monitoring in chat."),
            arrayOf("/cr execute", "<command>", "/cr exec, /cexec", "Execute a command as console."),
            arrayOf("/cr list", "", "/cr l", "List players monitoring the console."),
            arrayOf("/cr reload", "", "", "Reload the plugin config."),
            arrayOf("/cr version", "", "/cr ver", "Show plugin version."))

        val header = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
            .append("ConsoleReader Help").color(ChatColor.DARK_AQUA)
            .append(" ======------").color(ChatColor.DARK_GRAY)
        sender.spigot().sendMessage(*header.create())

        for (command in commands) {
            val helpEntry = ComponentBuilder("")
            val hoverText = ComponentBuilder("")

            val aliases = command[2].split(",")
            if (aliases[0].isNotEmpty()) {
                hoverText.append("Aliases: ").color(ChatColor.GRAY)
            }
            for (i in aliases.indices) {
                hoverText.append(aliases[i].trim()).color(ChatColor.DARK_AQUA)
                if (i != aliases.lastIndex) {
                    hoverText.append(", ").color(ChatColor.GRAY)
                } else if (aliases[i].isNotEmpty()) {
                    hoverText.append("\n")
                }
            }

            // Add ClickEvent and HoverEvent based on command arguments
            if (command[1].isEmpty() || command[1].matches(Regex("\\[(.*?)]"))){
                helpEntry.append(command[0]).color(ChatColor.DARK_AQUA)
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, command[0]))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.append("Click to run.").color(ChatColor.GRAY).create()))
            } else {
                helpEntry.append(command[0]).color(ChatColor.DARK_AQUA)
                    .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command[0]))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.append("Click to suggest.").color(ChatColor.GRAY).create()))
            }

            helpEntry.append(if (command[1].isEmpty()) "" else " " + command[1], ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA)
                .append(" - ").color(ChatColor.DARK_GRAY)
                .append(command[3]).color(ChatColor.GRAY)

            sender.spigot().sendMessage(*helpEntry.create())
        }
    }

    @Subcommand("read|r")
    @CommandPermission("consolereader.read")
    @CommandCompletion("@players")
    @Description("Toggle reading of console in chat")
    fun onRead(player: Player, @Optional otherPlayerName: String?) {
        if (otherPlayerName == null || player.name == otherPlayerName) {
            if (!LogAppenderManager.isReading(player)) {
                LogAppenderManager.startReading(player)
                player.sendPrefixMsg("Console reading enabled!")
            } else {
                LogAppenderManager.stopReading(player)
                player.sendPrefixMsg("Console reading disabled.")
            }
        } else {
            val otherPlayer: Player? = Bukkit.getPlayer(otherPlayerName)
            if (otherPlayer == null || !otherPlayer.isOnline) {
                player.sendPrefixMsg("Could not find player $otherPlayerName.")
                return
            }

            if (otherPlayer.hasPermission("consolereader.read")) {
                if (!LogAppenderManager.isReading(otherPlayer)) {
                    LogAppenderManager.startReading(otherPlayer)
                    player.sendPrefixMsg("Console reading enabled for $otherPlayerName!")
                    otherPlayer.sendPrefixMsg("Console reading enabled!")
                } else {
                    LogAppenderManager.stopReading(otherPlayer)
                    player.sendPrefixMsg("Console reading disabled for $otherPlayerName")
                    otherPlayer.sendPrefixMsg("Console reading disabled.")
                }
            } else {
                player.sendPrefixMsg("&cError: $otherPlayerName does not have permission to read console.")
            }
        }
    }

    @CommandAlias("cexec")
    @Subcommand("execute|exec")
    @CommandPermission("consolereader.execute")
    @CommandCompletion("<command>")
    @Description("Execute a command as console.")
    fun onExecute(player: Player, @Optional command: String?) {
        if (command == null || command.isEmpty()) {
            player.sendPrefixMsg("&cError: You need to specify a command.")
            return
        }

        for (forbiddenCommand in MainConfig.forbiddenCommands) {
            if (command.startsWith(forbiddenCommand, ignoreCase = true)) {
                player.sendPrefixMsg("&cError: The /$forbiddenCommand command may only be used in the real console.")
                return
            }
        }

        if (!LogAppenderManager.isReading(player)) {
            player.sendPrefixMsg("Temporarily enabling console reading for 5 seconds.")
            LogAppenderManager.startReadingTemp(player, 5000)
        }

        Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, command)
    }

    @Subcommand("list|l")
    @CommandPermission("consolereader.list")
    @Description("List players monitoring the console.")
    fun onList(sender: CommandSender) {
        val readingPlayerUUIDs = LogAppenderManager.getReadingPlayerUUIDs()
        val onlinePlayerNames = arrayListOf<String?>()
        val offlinePlayerNames = arrayListOf<String?>()
        var message = "Players: "
        for (uuid in readingPlayerUUIDs) {
            val player = Bukkit.getOfflinePlayer(uuid)
            if (player.isOnline) {
                onlinePlayerNames.add(player.name)
            } else {
                offlinePlayerNames.add(player.name)
            }
        }

        if (onlinePlayerNames.isEmpty() && offlinePlayerNames.isEmpty()) {
            message += "&7None"
        }

        for (name in onlinePlayerNames) {
            message += "&b$name&7, "
        }

        for (name in offlinePlayerNames) {
            message += "&8$name (offline)&7, "
        }

        sender.sendPrefixMsg(message)
    }

    @Subcommand("reload")
    @CommandPermission("consolereader.reload")
    @Description("Reload the plugin configuration.")
    fun onReload(sender: CommandSender) {
        MainConfig.reload()
        sender.sendPrefixMsg("Configuration reloaded.")
    }

    @Subcommand("version|ver")
    @CommandPermission("consolereader.read")
    @Description("See the plugin version.")
    fun onVersion(sender: CommandSender) {
        sender.sendPrefixMsg("ConsoleReader ${ConsoleReader.instance?.description?.version} by kyleseven")
    }
}