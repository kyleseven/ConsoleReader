package me.kyleseven.consolereader.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.kyleseven.consolereader.ConsoleReader;
import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.config.MainConfig;
import me.kyleseven.consolereader.logreader.LogAppenderManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("consolereader|cr")
public class MainCommand extends BaseCommand {
    @CatchUnknown
    public void onInvalid(CommandSender sender) {
        Utils.sendPrefixMsg(sender, "&cUnknown subcommand.");
    }

    @Subcommand("help|h")
    @CommandPermission("consolereader.read")
    @HelpCommand
    @Default
    public void onHelp(CommandSender sender) {
        String[] help = {"&8------====== &3ConsoleReader Help &8======------",
                "&3/cr help &8- &7Shows this help menu.",
                "&3/cr read [player]&8- &7Toggle console monitoring in chat.",
                "&3/cr execute <command> &8- &7Execute a command as console.",
                "&3/cr reload &8- &7Reload the plugin config.",
                "&3/cr version &8- &7Show plugin version"};

        for (String s : help) {
            Utils.sendMsg(sender, s);
        }
    }

    @Subcommand("read|r")
    @CommandPermission("consolereader.read")
    @CommandCompletion("@players")
    @Description("Toggle monitoring of the console in game.")
    public void onEnable(Player player, @Optional String playerName) {
        if (playerName == null || player.getName().equals(playerName)) {
            if (!LogAppenderManager.isReading(player)) {
                LogAppenderManager.startReading(player);
                Utils.sendPrefixMsg(player, "Console reading enabled!");
            } else {
                LogAppenderManager.stopReading(player);
                Utils.sendPrefixMsg(player, "Console reading disabled.");
            }
        } else {
            Player togglePlayer = Bukkit.getPlayer(playerName);
            if (togglePlayer == null || !togglePlayer.isOnline()) {
                Utils.sendPrefixMsg(player, "Could not find that player.");
                return;
            }

            if (player.hasPermission("consolereader.read")) {
                if (!LogAppenderManager.isReading(togglePlayer)) {
                    LogAppenderManager.startReading(togglePlayer);
                    Utils.sendPrefixMsg(player, "Console reading enabled for " + playerName + "!");
                    Utils.sendPrefixMsg(togglePlayer, "Console reading enabled by " + player.getName() + "!");
                } else {
                    LogAppenderManager.stopReading(togglePlayer);
                    Utils.sendPrefixMsg(player, "Console reading disabled for " + playerName + ".");
                    Utils.sendPrefixMsg(togglePlayer, "Console reading disabled by " + player.getName() + ".");
                }
            } else {
                Utils.sendPrefixMsg(player, "&cError: " + playerName + " does not have permission to read console.");
            }
        }
    }

    @CommandAlias("cexec")
    @Subcommand("execute|exec")
    @CommandPermission("consolereader.execute")
    @CommandCompletion("<command>")
    @Description("Execute a command as console.")
    public void onExecute(Player player, @Optional String command) {
        if (command == null || command.isEmpty()) {
            Utils.sendPrefixMsg(player, "&cError: You need to specify a command.");
            return;
        }

        // Check if command is forbidden
        for (String forbiddenCommand : MainConfig.getInstance().getForbiddenCommands()) {
            if (command.startsWith(forbiddenCommand)) {
                Utils.sendPrefixMsg(player, "That command must be executed using the actual console.");
                return;
            }
        }

        if (!LogAppenderManager.isReading(player)) {
            Utils.sendPrefixMsg(player, "Temporarily enabling console reading for 5 seconds.");
            LogAppenderManager.startReadingTemp(player, (long) 5000);
        }

        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);
    }

    @Subcommand("reload")
    @CommandPermission("consolereader.reload")
    @Description("Reload the plugin configuration.")
    public void onReload(CommandSender sender) {
        MainConfig.reload();
        Utils.sendPrefixMsg(sender, "Configuration reloaded.");
    }

    @Subcommand("version|ver")
    @CommandPermission("consolereader.read")
    @Description("See the plugin version.")
    public void onVersion(CommandSender sender) {
        String version = ConsoleReader.getPlugin().getDescription().getVersion();
        Utils.sendPrefixMsg(sender, "ConsoleReader " + version + " by kyleseven");
    }
}
