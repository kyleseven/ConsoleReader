package me.kyleseven.consolereader.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.logappender.LogAppenderManager
import me.kyleseven.consolereader.utils.sendPrefixMsg
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.concurrent.thread

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
        data class Command(
            val name: String = "/cr",
            val args: String = "",
            val aliases: ArrayList<String> = arrayListOf(),
            val description: String = "ConsoleReader command."
        )

        val commands = arrayOf(Command(name = "/cr help", aliases = arrayListOf("/cr", "/cr h"), description = "Shows this help menu."),
            Command(name = "/cr read", args = "[player]", aliases = arrayListOf("/cr r"), description = "Toggle console monitoring in chat."),
            Command(name = "/cr execute", args = "<command>", aliases = arrayListOf("/cr exec", "/cexec"), description = "Execute a command as console."),
            Command(name = "/cr list", aliases = arrayListOf("/cr l"), description = "List players monitoring the console."),
            Command(name = "/cr reload", aliases = arrayListOf("/cr rel"), description = "Reload the plugin config."),
            Command(name = "/cr version", aliases = arrayListOf("/cr ver"), description = "Show plugin version."))

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
            for (i in command.aliases.indices) {
                hoverText.append(command.aliases[i].trim()).color(ChatColor.DARK_AQUA)
                if (i != command.aliases.lastIndex) {
                    hoverText.append(", ").color(ChatColor.GRAY)
                } else if (command.aliases[i].isNotEmpty()) {
                    hoverText.append("\n")
                }
            }

            // Add ClickEvent and HoverEvent based on command arguments
            if (command.args.isEmpty() || command.args.matches(Regex("\\[(.*?)]"))){
                helpEntry.append(command.name).color(ChatColor.DARK_AQUA)
                    .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, command.name))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.append("Click to run.").color(ChatColor.GRAY).create()))
            } else {
                helpEntry.append(command.name).color(ChatColor.DARK_AQUA)
                    .event(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.name))
                    .event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText.append("Click to suggest.").color(ChatColor.GRAY).create()))
            }

            helpEntry.append(if (command.args.isEmpty()) "" else " ${command.args}", ComponentBuilder.FormatRetention.NONE).color(ChatColor.AQUA)
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
        if (otherPlayerName == null || sender.name == otherPlayerName) {
            if (sender is Player) {
                if (!LogAppenderManager.isReading(sender)) {
                    LogAppenderManager.startReading(sender)
                    sender.sendPrefixMsg("Console reading enabled!")
                } else {
                    LogAppenderManager.stopReading(sender)
                    sender.sendPrefixMsg("Console reading disabled.")
                }
            } else {
                sender.sendPrefixMsg("&cError: Console must specify a player.")
            }
        } else if (sender.hasPermission("consolereader.read.others")) {
            thread {
                // Use of deprecated function is necessary to get an OfflinePlayer from a name.
                @Suppress("DEPRECATION") val otherOfflinePlayer = Bukkit.getOfflinePlayer(otherPlayerName)
                if (!otherOfflinePlayer.hasPlayedBefore()) {
                    sender.sendPrefixMsg("&cError: That player hasn't joined this server before.")
                    return@thread
                }
                if (otherOfflinePlayer.isOnline) {
                    val otherPlayer = otherOfflinePlayer as Player
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
                        sender.sendPrefixMsg("&cError: ${otherPlayer.name} does not have permission to read console.")
                    }
                } else {
                    if (!LogAppenderManager.isReading(otherOfflinePlayer)) {
                        LogAppenderManager.startReading(otherOfflinePlayer)
                        sender.sendPrefixMsg("Console reading will be enabled for ${otherOfflinePlayer.name} upon login!")
                        sender.sendPrefixMsg("Note: If they lack the permission, console reading will not be enabled.")
                    } else {
                        LogAppenderManager.stopReading(otherOfflinePlayer)
                        sender.sendPrefixMsg("Console reading will be disabled for ${otherOfflinePlayer.name} upon login.")
                    }
                }
            }
        } else {
            sender.sendPrefixMsg("&cError: You do not have permission to toggle console reading for other players.")
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
            message += "&7None"
        }

        for (i in onlinePlayerNames.indices) {
            message += "&b${onlinePlayerNames[i]}"
            if (i != onlinePlayerNames.lastIndex || offlinePlayerNames.isNotEmpty()) {
                message += "&7, "
            }
        }

        for (i in offlinePlayerNames.indices) {
            message += "&8${offlinePlayerNames[i]} (offline)"
            if (i != offlinePlayerNames.lastIndex) {
                message += "&7, "
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
        sender.sendPrefixMsg("ConsoleReader ${ConsoleReader.instance?.description?.version} by kyleseven")
    }
}