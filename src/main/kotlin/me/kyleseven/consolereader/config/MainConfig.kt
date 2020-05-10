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
        get() = ChatColor.valueOf(config!!.getString("log_color")!!)

    val showChat: Boolean
        get() = config!!.getBoolean("show_chat")

    val showOwnCommands: Boolean
        get() = config!!.getBoolean("show_own_commands")

    val forbiddenCommands: List<String>
        get() = config!!.getStringList("forbidden_commands")

    fun reload() {
        loadFile()
    }
}