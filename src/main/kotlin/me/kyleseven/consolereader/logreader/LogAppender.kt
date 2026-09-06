package me.kyleseven.consolereader.logreader

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import java.io.PrintWriter
import java.io.StringWriter
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicLong

class LogAppender(queueCapacity: Int = DEFAULT_QUEUE_CAPACITY) :
    AbstractAppender(APPENDER_NAME, null, null, true, null) {

    data class Entry(
        val timeMillis: Long,
        val level: Level,
        val loggerName: String,
        val threadName: String,
        val message: String,
        val throwable: String?
    )

    data class DrainResult(val entries: List<Entry>, val droppedEntries: Long)

    private val entries = ArrayBlockingQueue<Entry>(queueCapacity)
    private val droppedEntries = AtomicLong()

    override fun append(event: LogEvent) {
        if (entries.remainingCapacity() == 0) {
            droppedEntries.incrementAndGet()
            return
        }

        val entry = Entry(
            timeMillis = event.timeMillis,
            level = event.level,
            loggerName = event.loggerName?.takeIf(String::isNotBlank) ?: "None",
            threadName = event.threadName ?: "Unknown",
            message = event.message?.formattedMessage.orEmpty(),
            throwable = event.thrown?.stackTraceString()
        )

        if (!entries.offer(entry)) {
            droppedEntries.incrementAndGet()
        }
    }

    fun drain(maxEntries: Int): DrainResult {
        val drained = ArrayList<Entry>(maxEntries)
        entries.drainTo(drained, maxEntries)
        return DrainResult(drained, droppedEntries.getAndSet(0))
    }

    fun clear() {
        entries.clear()
        droppedEntries.set(0)
    }

    private fun Throwable.stackTraceString(): String = StringWriter().also { writer ->
        printStackTrace(PrintWriter(writer))
    }.toString()

    companion object {
        const val APPENDER_NAME = "ConsoleReader"
        const val DEFAULT_QUEUE_CAPACITY = 1_000
    }
}
