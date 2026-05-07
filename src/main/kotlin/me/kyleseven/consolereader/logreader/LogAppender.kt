package me.kyleseven.consolereader.logreader

import net.md_5.bungee.api.ChatColor
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.regex.Pattern

class LogAppender(
    private val subscribers: Set<Player>,
    private val getGlobalFilters: () -> List<Pattern>,
    private val getPlayerFilters: (UUID) -> List<Pattern>
) : AbstractAppender("ConsoleReader", null, null, false, null) {

    private val queue = ConcurrentLinkedQueue<Pair<String, LogMessage>>()

    init {
        start()
    }

    override fun append(event: LogEvent?) {
        if (subscribers.isEmpty()) return

        val log = event!!.toImmutable()
        val strippedMsg = ChatColor.stripColor(log.message.formattedMessage)

        // Global filters — if matched, drop for all subscribers
        for (pattern in getGlobalFilters()) {
            if (pattern.matcher(strippedMsg).matches()) return
        }

        queue.add(strippedMsg to LogMessageBuilder.build(log))
    }

    fun drainQueue() {
        var item = queue.poll()
        while (item != null) {
            val (strippedMsg, msg) = item
            for (player in subscribers) {
                // Per-player filter check — skip this player if any of their patterns match
                val playerPatterns = getPlayerFilters(player.uniqueId)
                if (playerPatterns.any { it.matcher(strippedMsg).matches() }) continue
                player.spigot().sendMessage(msg.prefix, msg.body)
            }
            item = queue.poll()
        }
    }
}