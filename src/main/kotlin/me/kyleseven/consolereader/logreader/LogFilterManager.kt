package me.kyleseven.consolereader.logreader

import me.kyleseven.consolereader.config.MainConfig
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

object LogFilterManager {

    // Filters without %PLAYERNAME%
    @Volatile
    var globalFilters: List<Pattern> = emptyList()
        private set

    // Raw filter strings that contain %PLAYERNAME%
    @Volatile
    private var templateFilters: List<String> = emptyList()

    // Per-player compiled patterns
    private val playerFilters = ConcurrentHashMap<UUID, List<Pattern>>()

    fun load() {
        val (templates, globals) = MainConfig.regexFilters.partition { it.contains("%PLAYERNAME%") }
        globalFilters = globals.map { Pattern.compile(it) }
        templateFilters = templates
    }

    fun reload(subscribers: Collection<Player>) {
        load()
        for (player in subscribers) {
            playerFilters[player.uniqueId] = compileForPlayer(player.name)
        }
    }

    fun onPlayerSubscribe(player: Player) {
        playerFilters[player.uniqueId] = compileForPlayer(player.name)
    }

    fun onPlayerUnsubscribe(player: Player) {
        playerFilters.remove(player.uniqueId)
    }

    fun getPlayerFilters(uuid: UUID): List<Pattern> = playerFilters[uuid] ?: emptyList()

    private fun compileForPlayer(playerName: String): List<Pattern> =
        templateFilters.map { Pattern.compile(it.replace("%PLAYERNAME%", Pattern.quote(playerName))) }
}