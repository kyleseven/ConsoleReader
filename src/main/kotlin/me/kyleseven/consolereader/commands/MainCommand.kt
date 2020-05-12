package me.kyleseven.consolereader.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.logreader.LogAppenderManager
import me.kyleseven.consolereader.utils.sendColorMsg
import me.kyleseven.consolereader.utils.sendPrefixMsg
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

@CommandAlias("cr|consolereader")
@Description("Use the ConsoleReader plugin.")
class MainCommand : BaseCommand() {

    @Dependency
    private lateinit var plugin: Plugin

    @CatchUnknown
    fun onInvalid(sender: CommandSender) {
        sender.sendPrefixMsg("&cUnknown subcommand.")
    }

    @Subcommand("help|h")
    @CommandPermission("consolereader.read")
    @HelpCommand
    @Default
    fun onHelp(sender: CommandSender) {
        val help = arrayOf("&8------====== &3ConsoleReader Help &8======------",
                "&3/cr help &8- &7Shows this help menu.",
                "&3/cr read [player]&8- &7Toggle console monitoring in chat.",
                "&3/cr execute <command> &8- &7Execute a command as console.",
                "&3/cr reload &8- &7Reload the plugin config.",
                "&3/cr version &8- &7Show plugin version")
        for (s in help) {
            sender.sendColorMsg(s)
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
        sender.sendPrefixMsg("ConsoleReader ${plugin.description.version} by kyleseven")
    }
}