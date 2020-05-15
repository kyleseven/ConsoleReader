package me.kyleseven.consolereader.config

import net.md_5.bungee.api.ChatColor

object MainConfig : ConfigLoader("config.yml") {
    /*
    Config keys
     */
    val version: String
        get() = config!!.getString("version")!!

    val prefix: String
        get() = config!!.getString("prefix")!!

    val logColor: ChatColor
        get() {
            val colorString = config!!.getString("log_color")!!
            return if (colorString.length == 1) {
                ChatColor.getByChar(colorString[0])
            } else {
                ChatColor.valueOf(colorString)
            }
        }

    val forbiddenCommands: List<String>
        get() = config!!.getStringList("forbidden_commands")

    val regexFilters: List<String>
        get() = config!!.getStringList("filters")

    fun reload() {
        loadFile()
    }
}