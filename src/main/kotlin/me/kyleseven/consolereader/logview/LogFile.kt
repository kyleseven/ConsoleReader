package me.kyleseven.consolereader.logview

import me.kyleseven.consolereader.ConsoleReader
import org.bukkit.Bukkit
import java.io.*
import java.lang.Integer.min
import java.util.zip.GZIPInputStream
import kotlin.math.ceil

class LogFile(fileName: String) {
    var pages = 0
    private var lines = 0
    private var file: File

    init {
        file = getUnzippedFile(fileName)
        pages = calculatePages()
    }

    fun getLinesFromPage(page: Int): List<String> {
        if (page < 1 || page > pages) {
            return arrayListOf()
        }

        val content = arrayListOf<String>()
        val start = (page - 1) * 7
        val end = min(start + 7, lines)

        val fs = FileInputStream(file.absolutePath)
        val br = BufferedReader(InputStreamReader(fs))
        for (i in 0 until end) {
            val line = br.readLine()
            if (i >= start) {
                content.add(line)
            }
        }

        fs.close()
        br.close()

        return content
    }

    private fun calculatePages(): Int {
        file.forEachLine {
            lines++
        }

        return ceil(lines.toDouble() / 7.0).toInt()
    }

    private fun getUnzippedFile(logFileName: String): File {
        val logPath = Bukkit.getServer().worldContainer.absolutePath + File.separator + "logs" + File.separator + logFileName
        val tempPath = ConsoleReader.instance.dataFolder.absolutePath + File.separator + "temp" + File.separator + logFileName.removeSuffix(".gz")
        val buffer = ByteArray(1024)

        if (logFileName == "latest.log") {
            return File(logPath)
        }

        try {
            val gzipIS = GZIPInputStream(FileInputStream(logPath))
            val outputFile = File(tempPath)
            outputFile.parentFile.mkdirs()
            val outputStream = FileOutputStream(outputFile)

            while (true) {
                val length = gzipIS.read(buffer)
                if (length < 0) break
                outputStream.write(buffer, 0, length)
            }

            gzipIS.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return File(tempPath)
    }
}