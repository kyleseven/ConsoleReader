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
                "&3/cr read &8- &7Toggle console monitoring in chat.",
                "&3/cr execute <command> &8- &7Execute a command as console.",
                "&3/cr reload &8- &7Reload the plugin config.",
                "&3/cr version &8- &7Show plugin version"};

        for (String s : help) {
            Utils.sendMsg(sender, s);
        }
    }

    @Subcommand("read|r")
    @CommandPermission("consolereader.read")
    @Description("Toggle monitoring of the console in game.")
    public void onEnable(Player player) {
        if (!LogAppenderManager.isReading(player)) {
            LogAppenderManager.startReading(player);
            Utils.sendPrefixMsg(player, "Console reading enabled!");
        } else {
            LogAppenderManager.stopReading(player);
            Utils.sendPrefixMsg(player, "Console reading disabled.");
        }
    }

    @CommandAlias("cexec")
    @Subcommand("execute|exec")
    @CommandPermission("consolereader.execute")
    @CommandCompletion("<command>")
    @Description("Execute a command as console.")
    public void onExecute(Player player, @Optional String command) {
        boolean needsTemporaryRead = !LogAppenderManager.isReading(player);

        if (command == null || command.isEmpty()) {
            Utils.sendPrefixMsg(player, "&cError: You need to specify a command.");
            return;
        }

        // Check if command is forbidden
        for (String forbiddenCommand : MainConfig.getInstance().getForbiddenCommands()) {
            String[] args = command.split(" ");
            String[] forbidden = forbiddenCommand.split(" ");
            boolean isForbiddenCommand = false;

            int i = 0;
            while (i < args.length && i < forbidden.length) {
                if (args[i].equals(forbidden[i])) {
                    i++;
                    if (i == forbidden.length) {
                        isForbiddenCommand = true;
                        break;
                    }
                } else {
                    break;
                }
            }

            if (isForbiddenCommand) {
                Utils.sendPrefixMsg(player, "That command must be executed using the actual console.");
                return;
            }
        }

        if (needsTemporaryRead) {
            Utils.sendPrefixMsg(player, "Temporarily enabling console reading for 5 seconds.");
            LogAppenderManager.startReading(player);
        }

        Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), command);

        if (needsTemporaryRead) {
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                LogAppenderManager.stopReading(player);
            }).start();
        }
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
