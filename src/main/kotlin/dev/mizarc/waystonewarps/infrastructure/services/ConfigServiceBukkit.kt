package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType
import org.bukkit.configuration.file.FileConfiguration

class ConfigServiceBukkit(val configFile: FileConfiguration): ConfigService {

    override fun getWarpLimit(): Int {
        return configFile.getInt("warp_limit", 3)
    }

    override fun getTeleportTimer(): Int {
        return configFile.getInt("teleport_timer", 5)
    }

    override fun getTeleportCostType(): CostType {
        return CostType.valueOf(configFile.getString("teleport_cost_type", "ITEM").toString())
    }

    override fun getTeleportCostAmount(): Int {
        return configFile.getInt("teleport_cost_amount", 3)
    }
}