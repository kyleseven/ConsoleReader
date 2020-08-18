package me.kyleseven.consolereader.logview

import me.kyleseven.consolereader.ConsoleReader
import me.kyleseven.consolereader.ui.Page
import me.kyleseven.consolereader.ui.sendPage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.io.File
import java.nio.file.Paths

object LogFileManager {
    lateinit var logList: Array<String>

    fun setup() {
        val logPath = Paths.get(Bukkit.getServer().worldContainer.absolutePath, "logs").toString()
        logList = File(logPath).list()?.filterNotNull()?.toTypedArray()?.sortedArrayDescending() ?: arrayOf()
        for (i in logList.indices) {
            logList[i] = logList[i].removeSuffix(".log.gz").removeSuffix(".log")
        }
    }

    fun sendPage(sender: CommandSender, logFileName: String, page: Int) {
        val logFile = LogFile(logFileName)
        val content = logFile.getLinesFromPage(page)

        val pageUI = Page(
            title = logFileName,
            content = content,
            pageNumber = page,
            maxPageNumber = logFile.pages,
            prevCmd = "/cr log view $logFileName ${page - 1}",
            nextCmd = "/cr log view $logFileName ${page + 1}"
        )

        sender.sendPage(pageUI)
    }

    fun cleanUp() {
        val tempDir = Paths.get(ConsoleReader.instance.dataFolder.absolutePath, "temp").toString()
        val tempFile = File(tempDir)
        tempFile.deleteRecursively()
    }
}