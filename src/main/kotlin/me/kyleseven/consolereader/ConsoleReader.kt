package me.kyleseven.consolereader

import org.bukkit.plugin.java.JavaPlugin

class ConsoleReader : JavaPlugin() {
    companion object {
        var instance: ConsoleReader? = null
        private set
    }

    override fun onEnable() {
        instance = this

    }

    override fun onDisable() {

    }

    private fun loadConfigs() {

    }

    private fun registerCommands() {

    }

    private fun registerEvents() {

    }
}