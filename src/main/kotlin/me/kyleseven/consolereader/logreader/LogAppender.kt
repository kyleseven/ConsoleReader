package me.kyleseven.consolereader.logreader

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

// Copy of a log event. Log4j can reuse event objects, so what is needed is copied here.
class LogLine(event: LogEvent) {
    val timeMillis: Long = event.timeMillis
    val level: Level = event.level
    val loggerName: String = event.loggerName.orEmpty()
    val threadName: String = event.threadName.orEmpty()
    val message: String = event.message?.formattedMessage.orEmpty()
}

class LogAppender : AbstractAppender("ConsoleReader", null, null, true, Property.EMPTY_ARRAY) {
    companion object {
        // Lines kept while waiting to be sent. The oldest lines are dropped beyond this.
        private const val MAX_QUEUED_LINES = 500
    }

    private val queue = ConcurrentLinkedQueue<LogLine>()
    private val queuedCount = AtomicInteger()
    private val droppedCount = AtomicInteger()

    // Off while nobody is reading
    @Volatile
    var accepting = false

    init {
        start()
    }

    /*
    This can be called from any thread, so the log event is only queued here.
    LogAppenderManager takes lines from the queue on the main thread and sends them to readers.
     */
    override fun append(event: LogEvent?) {
        if (!accepting || event == null) {
            return
        }

        queue.offer(LogLine(event))

        if (queuedCount.incrementAndGet() > MAX_QUEUED_LINES && queue.poll() != null) {
            queuedCount.decrementAndGet()
            droppedCount.incrementAndGet()
        }
    }

    fun poll(): LogLine? {
        val line = queue.poll()
        if (line != null) {
            queuedCount.decrementAndGet()
        }
        return line
    }

    // Number of lines dropped since the last call
    fun takeDroppedCount(): Int {
        return droppedCount.getAndSet(0)
    }

    fun clear() {
        while (poll() != null) {
            // Emptying the queue
        }
        droppedCount.set(0)
    }
}
