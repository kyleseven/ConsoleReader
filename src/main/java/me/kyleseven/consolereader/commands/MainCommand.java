package me.kyleseven.consolereader.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.kyleseven.consolereader.Utils;
import me.kyleseven.consolereader.config.MainConfig;
import me.kyleseven.consolereader.logreader.LogListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

@CommandAlias("consolereader|cr")
public class MainCommand extends BaseCommand {

    private static final HashMap<UUID, LogListener> listeningPlayers = new HashMap<>();

    public static HashMap<UUID, LogListener> getListeningPlayers() {
        return listeningPlayers;
    }

    @CatchUnknown
    public void onInvalid(CommandSender sender) {
        Utils.sendPrefixMsg(sender, "&cUnknown subcommand.");
    }

    @Subcommand("help|h")
    @CommandPermission("consolereader.use")
    @HelpCommand
    @Default
    public void onHelp(CommandSender sender) {
        String[] help = {"&8-----===== &3ConsoleReader Help &8=====-----",
                "&3/cr help &8- &7Shows this help menu.",
                "&3/cr enable &8- &7Enable reading console in chat.",
                "&3/cr disable &8- &7Disable reading console in chat.",
                "&3/cr execute <command> &8- &7Execute a command as console.",
                "&3/cr reload &8- &7Reload the plugin config.",
                "&3/cr version &8- &7Show plugin version"};

        for (String s : help) {
            Utils.sendMsg(sender, s);
        }
    }

    @Subcommand("enable|on")
    @CommandPermission("consolereader.read")
    @Description("Enable reading of the console in game.")
    public void onEnable(Player player) {
        if (listeningPlayers.get(player.getUniqueId()) != null) {
            Utils.sendPrefixMsg(player, "Console reading is already enabled.");
            return;
        }

        listeningPlayers.put(player.getUniqueId(), new LogListener(player));
        listeningPlayers.get(player.getUniqueId()).startReading();
        Utils.sendPrefixMsg(player, "Console reading enabled!");
    }

    @Subcommand("disable|off")
    @CommandPermission("consolereader.read")
    @Description("Disable reading of the console in game.")
    public void onDisable(Player player) {
        if (listeningPlayers.get(player.getUniqueId()) == null) {
            Utils.sendPrefixMsg(player, "Console reading is already disabled.");
            return;
        }

        listeningPlayers.get(player.getUniqueId()).stopReading();
        listeningPlayers.remove(player.getUniqueId());
        Utils.sendPrefixMsg(player, "Console reading disabled.");
    }

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
        Utils.sendPrefixMsg(sender, "ConsoleReader " + MainConfig.getInstance().getVersion() + " by kyleseven");
    }
}
