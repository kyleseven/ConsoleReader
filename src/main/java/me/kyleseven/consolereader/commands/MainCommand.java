package me.kyleseven.consolereader.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.kyleseven.consolereader.ConsoleReader;
import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.config.MainConfig;
import me.kyleseven.consolereader.logreader.LogAppenderManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

@CommandAlias("consolereader|cr")
public class MainCommand extends BaseCommand {
    @CatchUnknown
    public void onInvalid(CommandSender sender) {
        Utils.sendPrefixMsg(sender, "&cUnknown subcommand.");
    }

    @Subcommand("help|h")
    @CommandPermission("consolereader.use")
    @HelpCommand
    @Default
    public void onHelp(CommandSender sender) {
        String[] help = {"&8------====== &3ConsoleReader Help &8======------",
                "&3/cr help &8- &7Shows this help menu.",
                "&3/cr monitor &8- &7Toggle console monitoring in chat.",
                "&3/cr execute <command> &8- &7Execute a command as console.",
                "&3/cr reload &8- &7Reload the plugin config.",
                "&3/cr version &8- &7Show plugin version"};

        for (String s : help) {
            Utils.sendMsg(sender, s);
        }
    }

    @Subcommand("monitor|mon")
    @CommandPermission("consolereader.read")
    @Description("Toggle monitoring of the console in game.")
    public void onEnable(Player player) {
        if (!LogAppenderManager.isReading(player)) {
            LogAppenderManager.startReading(player);
            Utils.sendPrefixMsg(player, "Console monitoring enabled!");
        }
        else {
            LogAppenderManager.stopReading(player);
            Utils.sendPrefixMsg(player, "Console monitoring disabled.");
        }
    }

    @CommandAlias("cexec")
    @Subcommand("execute|exec")
    @CommandPermission("consolereader.execute")
    @Description("Execute a command as console.")
    public void onExecute(Player player, String[] args) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        StringBuilder command = new StringBuilder();
        for (String s : args) {
            command.append(s).append(" ");
        }
        Bukkit.dispatchCommand(console, command.toString());
    }

    @Subcommand("reload")
    @CommandPermission("consolereader.reload")
    @Description("Reload the plugin configuration.")
    public void onReload(CommandSender sender) {
        MainConfig.reload();
        Utils.sendPrefixMsg(sender, "Configuration reloaded.");
    }

    @Subcommand("version|ver")
    @CommandPermission("consolereader.use")
    @Description("See the plugin version.")
    public void onVersion(CommandSender sender) {
        String version = ConsoleReader.getPlugin().getDescription().getVersion();
        Utils.sendPrefixMsg(sender, "ConsoleReader " + version + " by kyleseven");
    }
}
