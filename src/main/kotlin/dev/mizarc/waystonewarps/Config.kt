package dev.mizarc.waystonewarps

import org.bukkit.Location
import org.bukkit.plugin.Plugin
import dev.mizarc.waystonewarps.utils.LocationConversions

class Config(val plugin: Plugin) {
    private val configFile = plugin.config

    var spawnLocation: String = ""
    var spawnTimer = 0
    var spawnCooldown = 0
    var homeDefaultToSpawn = false
    var homeTimer = 0
    var homeCooldown = 0
    var inviteRange = 0
    var homeLimit = 0
    var spawnCost = 0
    var homeCost = 0
    var warpCost = 0
    var warpTimer = 0
    var warpCooldown = 0

    init {
        createDefaultConfig()
        loadConfig()
    }

    fun loadConfig() {
        spawnTimer = configFile.getInt("spawn_timer")
        spawnCooldown = configFile.getInt("spawn_cooldown")
        homeDefaultToSpawn = configFile.getBoolean("home_default_to_spawn")
        homeTimer = configFile.getInt("home_timer")
        homeCooldown = configFile.getInt("home_cooldown")
        inviteRange = configFile.getInt("invite_range")
        spawnLocation = configFile.getString("spawn_location")!!
        homeLimit = configFile.getInt("home_limit")
        spawnCost = configFile.getInt("spawn_cost")
        homeCost = configFile.getInt("home_cost")
        warpCost = configFile.getInt("warp_cost")
        warpTimer = configFile.getInt("warp_timer")
        warpCooldown = configFile.getInt("warp_cooldown")
    }

    fun setSpawnLocation(location: Location) {
        val spawnLocationString = LocationConversions.locationToString(location)
        spawnLocation = spawnLocationString
        configFile.set("spawn_location", spawnLocationString)
        plugin.saveConfig()
    }

    private fun createDefaultConfig() {
        plugin.config.addDefault("spawn_location" , "")
        plugin.config.addDefault("spawn_cost", 2)
        plugin.config.addDefault("spawn_timer", 5)
        plugin.config.addDefault("spawn_cooldown", 0)
        plugin.config.addDefault("home_limit", 3)
        plugin.config.addDefault("home_cost", 4)
        plugin.config.addDefault("home_timer", 5)
        plugin.config.addDefault("home_cooldown", 0)
        plugin.config.addDefault("home_default_to_spawn", true)
        plugin.config.addDefault("warp_cost", 8)
        plugin.config.addDefault("warp_timer", 6)
        plugin.config.addDefault("warp_cost", 6)
        plugin.config.addDefault("warp_cooldown", 0)
        plugin.config.addDefault("tpr_radius", 1000)
        plugin.config.addDefault("tpr_attempts", 50)
        plugin.config.options().copyDefaults(true)
        plugin.saveConfig()
    }
}