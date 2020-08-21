package me.kyleseven.consolereader.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.logappender.LogAppenderManager
import me.kyleseven.consolereader.logview.LogFileManager
import me.kyleseven.consolereader.ui.Command
import me.kyleseven.consolereader.ui.Page
import me.kyleseven.consolereader.ui.sendHelpMenu
import me.kyleseven.consolereader.ui.sendPage
import me.kyleseven.consolereader.utils.sendPrefixMsg
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.concurrent.thread
import kotlin.math.ceil
import kotlin.math.min

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
        val commands = arrayOf(
            Command(
                name = "/cr help",
                aliases = arrayListOf("/cr", "/cr h"),
                description = "Shows this help menu."
            ),
            Command(
                name = "/cr read",
                args = "[player]",
                aliases = arrayListOf("/cr r"),
                description = "Toggle console monitoring in chat."
            ),
            Command(
                name = "/cr execute",
                args = "<command>",
                aliases = arrayListOf("/cr exec", "/cexec"),
                description = "Execute a command as console."
            ),
            Command(
                name = "/cr list",
                aliases = arrayListOf("/cr l"),
                description = "List players monitoring the console."
            ),
            Command(
                name = "/cr log",
                args = "<list | view>",
                description = "View previous server logs."
            ),
            Command(
                name = "/cr reload",
                aliases = arrayListOf("/cr rel"),
                description = "Reload the plugin config."
            ),
            Command(
                name = "/cr version",
                aliases = arrayListOf("/cr ver"),
                description = "Show plugin version."
            )
        )

        sender.sendHelpMenu(*commands)
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
                sender.sendPrefixMsg("${ChatColor.RED}Error: Console must specify a player.")
            }
        } else if (sender.hasPermission("consolereader.read.others")) {
            thread {
                // Use of deprecated function is necessary to get an OfflinePlayer from a name.
                @Suppress("DEPRECATION") val otherOfflinePlayer = Bukkit.getOfflinePlayer(otherPlayerName)
                if (!otherOfflinePlayer.hasPlayedBefore()) {
                    sender.sendPrefixMsg("${ChatColor.RED}Error: That player hasn't joined this server before.")
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
                        sender.sendPrefixMsg("${ChatColor.RED}Error: ${otherPlayer.name} does not have permission to read console.")
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
            sender.sendPrefixMsg("${ChatColor.RED}Error: You do not have permission to toggle console reading for other players.")
        }
    }

    @CommandAlias("cexec")
    @Subcommand("execute|exec")
    @CommandPermission("consolereader.execute")
    @CommandCompletion("<command>")
    @Description("Execute a command as console.")
    fun onExecute(player: Player, @Optional command: String?) {
        if (command == null || command.isEmpty()) {
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

    @Subcommand("log")
    @CommandPermission("consolereader.log")
    @Description("See a previous server log.")
    @Suppress("RedundantInnerClassModifier")
    inner class LogCommand : BaseCommand() {
        @Default
        @Subcommand("help|h")
        @Description("Shows help for the log subcommands")
        fun onLogHelp(sender: CommandSender) {
            val commands = arrayOf(
                Command(
                    name = "/cr log help",
                    description = "Shows this help menu."
                ),
                Command(
                    name = "/cr log list",
                    args = "[page]",
                    aliases = arrayListOf("/cr log l"),
                    description = "List all available logs."
                ),
                Command(
                    name = "/cr log view",
                    args = "<logName> [page]",
                    description = "View the contents of the specified log."
                )
            )

            sender.sendHelpMenu(*commands)
        }

        @Subcommand("list|l")
        @Description("List all available logs.")
        fun onLogList(sender: CommandSender, page: Int) {
            val totalPages = ceil(LogFileManager.logList.size / 7.0).toInt()
            val start = (page - 1) * 7
            val end = min(start + 7, LogFileManager.logList.lastIndex)
            val content = ArrayList<String>()
            for (i in start..end) {
                content.add(LogFileManager.logList[i])
            }

            val pageUI = Page(
                title = "Log List",
                content = content,
                pageNumber = page,
                maxPageNumber = totalPages,
                prevCmd = "/cr list ${page - 1}",
                nextCmd = "/cr list ${page + 1}"
            )

            sender.sendPage(pageUI)
        }

        @Subcommand("view")
        @Description("Look at a previous log")
        fun onLogView(sender: CommandSender, fileName: String, page: Int) {
            if (!LogFileManager.logList.contains(fileName)) {
                sender.sendPrefixMsg("${ChatColor.RED}Error: Could not find that log file.")
                return
            }

            LogFileManager.sendPage(sender, if (fileName == "latest") "$fileName.log" else "$fileName.log.gz", page)
        }
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