package dev.mizarc.waystonewarps.infrastructure.persistence
import org.bukkit.plugin.Plugin

class Config(val plugin: Plugin) {
    private val configFile = plugin.config

    var warpLimit = 0
    var teleportCost = 0
    var teleportTimer = 0

    init {
        createDefaultConfig()
        loadConfig()
    }

    private fun loadConfig() {
        warpLimit = configFile.getInt("warp_limit")
        teleportCost = configFile.getInt("teleport_cost")
        teleportTimer = configFile.getInt("teleport_timer")
    }

    private fun createDefaultConfig() {
        plugin.config.addDefault("warp_limit", 3)
        plugin.config.addDefault("teleport_cost", 4)
        plugin.config.addDefault("teleport_timer", 5)
        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
    }
}