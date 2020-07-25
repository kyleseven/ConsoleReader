package me.kyleseven.consolereader.logview

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.config.MainConfig
import me.kyleseven.consolereader.utils.sendColorMsg
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Bukkit.spigot
import org.bukkit.command.CommandSender
import java.io.File

object LogFileManager {
    lateinit var logList: Array<String>

    fun setup() {
        val logPath = Bukkit.getServer().worldContainer.absolutePath + File.separator + "logs"
        logList = File(logPath).list()?.filterNotNull()?.toTypedArray() ?: arrayOf()
        for (i in logList.indices) {
            logList[i] = logList[i].removeSuffix(".log.gz").removeSuffix(".log")
        }
    }

    fun sendPage(sender: CommandSender, logFileName: String, page: Int) {
        val logFile = LogFile(logFileName)
        val content = logFile.getLinesFromPage(page)

        val header = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
            .append(logFileName).color(ChatColor.DARK_AQUA)
            .append(" ======------").color(ChatColor.DARK_GRAY)

        val footer = ComponentBuilder("------====== ").color(ChatColor.DARK_GRAY)
            .append("$page of ${logFile.pages}").color(ChatColor.DARK_AQUA)
            .append(" ======------").color(ChatColor.DARK_GRAY)

        sender.spigot().sendMessage(*header.create())
        for (line in content) {
            sender.sendColorMsg(MainConfig.logColor.toString() + line)
        }
        sender.spigot().sendMessage(*footer.create())
    }

    fun cleanUp() {
        val tempDir = ConsoleReader.instance.dataFolder.absolutePath + File.separator + "temp"
        val tempFile = File(tempDir)
        tempFile.deleteRecursively()
    }
}