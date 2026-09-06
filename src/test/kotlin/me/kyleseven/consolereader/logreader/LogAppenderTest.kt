package me.kyleseven.consolereader.logreader

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.impl.Log4jLogEvent
import org.apache.logging.log4j.message.SimpleMessage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LogAppenderTest {
    @Test
    fun `captures event data and reports queue overflow`() {
        val appender = LogAppender(queueCapacity = 1)
        val event = Log4jLogEvent.newBuilder()
            .setTimeMillis(123L)
            .setLevel(Level.ERROR)
            .setLoggerName("test.logger")
            .setThreadName("worker-thread")
            .setMessage(SimpleMessage("failed"))
            .setThrown(IllegalStateException("boom"))
            .build()

        appender.append(event)
        appender.append(event)

        val result = appender.drain(10)
        assertEquals(1, result.entries.size)
        assertEquals(1, result.droppedEntries)
        assertEquals("failed", result.entries.single().message)
        assertEquals("test.logger", result.entries.single().loggerName)
        assertTrue(result.entries.single().throwable.orEmpty().contains("IllegalStateException: boom"))
    }

    @Test
    fun `drain respects its per-tick limit`() {
        val appender = LogAppender(queueCapacity = 3)
        repeat(3) { index ->
            appender.append(
                Log4jLogEvent.newBuilder()
                    .setLevel(Level.INFO)
                    .setMessage(SimpleMessage(index.toString()))
                    .build()
            )
        }

        assertEquals(listOf("0", "1"), appender.drain(2).entries.map { it.message })
        assertEquals(listOf("2"), appender.drain(2).entries.map { it.message })
    }

    @Test
    fun `captures large messages without truncation`() {
        val appender = LogAppender(queueCapacity = 1)
        val message = "x".repeat(40_000)
        val event = Log4jLogEvent.newBuilder()
            .setLevel(Level.INFO)
            .setMessage(SimpleMessage(message))
            .build()

        appender.append(event)

        assertEquals(message, appender.drain(1).entries.single().message)
    }
}
