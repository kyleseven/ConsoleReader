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
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("cr|consolereader")
@Description("Use the ConsoleReader plugin.")
class MainCommand : BaseCommand() {
    @CatchUnknown
    fun onInvalid(sender: CommandSender) {
        sender.sendPrefixMsg("${ChatColor.RED}Unknown subcommand.")
    }

    @Subcommand("help|h")
    @CommandPermission("consolereader.read")
    @HelpCommand
    @Default
    fun onHelp(sender: CommandSender) {
        data class Command(
            val name: String = "/cr",
            val args: String = "",
            val aliases: List<String> = listOf(),
            val description: String = "ConsoleReader command."
        )

        val commands = arrayOf(
            Command(
                name = "/cr help",
                aliases = listOf("/cr", "/cr h"),
                description = "Shows this help menu."
            ),
            Command(
                name = "/cr read",
                args = "[player]",
                aliases = listOf("/cr r"),
                description = "Toggle console monitoring in chat."
            ),
            Command(
                name = "/cr execute",
                args = "<command>",
                aliases = listOf("/cr exec", "/cexec"),
                description = "Execute a command as console."
            ),
            Command(
                name = "/cr list",
                aliases = listOf("/cr l"),
                description = "List players monitoring the console."
            ),
            Command(
                name = "/cr reload",
                aliases = listOf("/cr rel"),
                description = "Reload the plugin config."
            ),
            Command(
                name = "/cr version",
                aliases = listOf("/cr ver"),
                description = "Show plugin version."
            )
        )

        val header = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
            .append("ConsoleReader Help").color(ChatColor.DARK_AQUA)
            .append(" ======------").color(ChatColor.DARK_GRAY)

        sender.spigot().sendMessage(*header.create())

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
                            Text(hoverText.append("Click to run.").color(ChatColor.GRAY).create())
                        )
                    )
            } else {
                helpEntry.append(command.name).color(ChatColor.DARK_AQUA)
                    .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.name))
                    .event(
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            Text(hoverText.append("Click to suggest.").color(ChatColor.GRAY).create())
                        )
                    )
            }

            helpEntry.append(" ${command.args}".ifBlank { "" }, ComponentBuilder.FormatRetention.NONE)
                .color(ChatColor.AQUA)
                .append(" - ").color(ChatColor.DARK_GRAY)
                .append(command.description).color(ChatColor.GRAY)

            sender.spigot().sendMessage(*helpEntry.create())
        }
    }

    @Subcommand("read|r")
    @CommandPermission("consolereader.read")
    @CommandCompletion("@players")
    @Description("Toggle reading of console in chat")
    fun onRead(sender: CommandSender, @Optional otherPlayerName: String?) {
        fun handleSelfToggle(sender: CommandSender) {
            if (sender is Player) {
                if (!LogAppenderManager.isReading(sender)) {
                    LogAppenderManager.startReading(sender)
                    sender.sendPrefixMsg("Console reading enabled!")
                } else {
                    LogAppenderManager.stopReading(sender)
                    sender.sendPrefixMsg("Console reading disabled.")
                }
            } else {
                sender.sendPrefixMsg("${ChatColor.RED}Error: Console must specify a player.")
            }
        }

        fun handleOtherOnlinePlayerToggle(sender: CommandSender, otherPlayer: Player) {
            if (otherPlayer.hasPermission("consolereader.read")) {
                if (!LogAppenderManager.isReading(otherPlayer)) {
                    LogAppenderManager.startReading(otherPlayer)
                    sender.sendPrefixMsg("Console reading enabled for ${otherPlayer.name}!")
                    otherPlayer.sendPrefixMsg("Console reading enabled!")
                } else {
                    LogAppenderManager.stopReading(otherPlayer)
                    sender.sendPrefixMsg("Console reading disabled for ${otherPlayer.name}")
                    otherPlayer.sendPrefixMsg("Console reading disabled.")
                }
            } else {
                sender.sendPrefixMsg("${ChatColor.RED}Error: ${otherPlayer.name} does not have permission to read console.")
            }
        }

        fun handleOtherOfflinePlayerToggle(sender: CommandSender, otherOfflinePlayer: OfflinePlayer) {
            if (!LogAppenderManager.isReading(otherOfflinePlayer)) {
                LogAppenderManager.startReading(otherOfflinePlayer)
                sender.sendPrefixMsg("Console reading will be enabled for ${otherOfflinePlayer.name} upon login!")
                sender.sendPrefixMsg("Note: If they lack the permission, console reading will not be enabled.")
            } else {
                LogAppenderManager.stopReading(otherOfflinePlayer)
                sender.sendPrefixMsg("Console reading will be disabled for ${otherOfflinePlayer.name} upon login.")
            }
        }

        fun handleOtherToggle(sender: CommandSender, otherPlayerName: String) {
            if (sender.hasPermission("consolereader.read.others")) {
                Bukkit.getScheduler().runTaskAsynchronously(ConsoleReader.instance, Runnable {
                    // Use of deprecated function is necessary to get an OfflinePlayer from a name.
                    @Suppress("DEPRECATION") val otherOfflinePlayer = Bukkit.getOfflinePlayer(otherPlayerName)
                    if (!otherOfflinePlayer.hasPlayedBefore()) {
                        sender.sendPrefixMsg("${ChatColor.RED}Error: That player hasn't joined this server before.")
                        return@Runnable
                    }

                    if (otherOfflinePlayer.isOnline) {
                        handleOtherOnlinePlayerToggle(sender, otherOfflinePlayer as Player)
                    } else {
                        handleOtherOfflinePlayerToggle(sender, otherOfflinePlayer)
                    }
                })
            } else {
                sender.sendPrefixMsg("${ChatColor.RED}Error: You do not have permission to toggle console reading for other players.")
            }
        }

        if (otherPlayerName == null || sender.name == otherPlayerName) {
            handleSelfToggle(sender)
        } else {
            handleOtherToggle(sender, otherPlayerName)
        }
    }

    @CommandAlias("cexec")
    @Subcommand("execute|exec")
    @CommandPermission("consolereader.execute")
    @CommandCompletion("<command>")
    @Description("Execute a command as console.")
    fun onExecute(player: Player, @Optional command: String?) {
        if (command.isNullOrEmpty()) {
            player.sendPrefixMsg("${ChatColor.RED}Error: You need to specify a command.")
            return
        }

        for (forbiddenCommand in MainConfig.forbiddenCommands) {
            if (command.startsWith(forbiddenCommand, ignoreCase = true)) {
                player.sendPrefixMsg("${ChatColor.RED}Error: The /$forbiddenCommand command may only be used in the real console.")
                return
            }
        }

        if (!LogAppenderManager.isReading(player)) {
            player.sendPrefixMsg("Temporarily enabling console reading for 5 seconds.")
            LogAppenderManager.startReadingTemp(player, 5)
        }

        Bukkit.dispatchCommand(Bukkit.getServer().consoleSender, command)
    }

    @Subcommand("list|l")
    @CommandPermission("consolereader.read")
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
            message += "${ChatColor.GRAY}None"
        }

        for ((i, playerName) in onlinePlayerNames.withIndex()) {
            message += "${ChatColor.AQUA}${playerName}"
            if (i != onlinePlayerNames.lastIndex || offlinePlayerNames.isNotEmpty()) {
                message += "${ChatColor.GRAY}, "
            }
        }

        for ((i, playerName) in offlinePlayerNames.withIndex()) {
            message += "${ChatColor.DARK_GRAY}${playerName} (offline)"
            if (i != offlinePlayerNames.lastIndex) {
                message += "${ChatColor.GRAY}, "
            }
        }

        sender.sendPrefixMsg(message)
    }

    @Subcommand("reload|rel")
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
        sender.sendPrefixMsg("ConsoleReader ${ConsoleReader.instance.description.version} by kyleseven")
    }
}