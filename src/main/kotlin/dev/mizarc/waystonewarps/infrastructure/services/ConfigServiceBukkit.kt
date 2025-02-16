package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class ConfigServiceBukkit(private val plugin: Plugin, private val configFile: FileConfiguration): ConfigService {

    init {
        createDefaultConfig()
    }

    override fun getWarpLimit(): Int {
        return configFile.getInt("warp_limit", 3)
    }

    override fun getTeleportTimer(): Int {
        return configFile.getInt("teleport_timer", 5)
    }

    override fun getTeleportCostType(): CostType {
        return CostType.valueOf(configFile.getString("teleport_cost_type", "ITEM").toString())
    }

    override fun getTeleportCostItem(): String {
        return configFile.getString("teleport_cost_item", "ENDER_PEARL").toString()
    }

    override fun getTeleportCostAmount(): Double {
        return configFile.getDouble("teleport_cost_amount", 3.0)
    }

    private fun createDefaultConfig() {
        val configFile = File(plugin.dataFolder, "config.yml")
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false)
        }
    }
}