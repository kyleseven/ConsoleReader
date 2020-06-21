package me.kyleseven.consolereader.config

import net.md_5.bungee.api.ChatColor

object MainConfig : ConfigLoader("config.yml") {
    /*
    Config keys
     */
    val version: String
        get() {
            val default = "INVALID"
            return config.getString("version") ?: default
        }

    val prefix: String
        get() {
            val default = "&8[&3CR&8] &7"
            return config.getString("prefix") ?: default
        }

    val logColor: ChatColor
        get() {
            val default = "GRAY"
            val colorString = config.getString("log_color") ?: default

            return try {
                if (colorString.length == 1) {
                    ChatColor.getByChar(colorString[0])
                } else {
                    ChatColor.valueOf(colorString)
                }
            } catch (e: Exception) {
                ChatColor.valueOf(default)
            }
        }

    val forbiddenCommands: List<String>
        get() = config.getStringList("forbidden_commands")

    val regexFilters: List<String>
        get() = config.getStringList("filters")

    fun reload() {
        loadFile()
    }
}