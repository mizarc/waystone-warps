package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import org.bukkit.configuration.file.FileConfiguration

class ConfigServiceBukkit(val configFile: FileConfiguration): ConfigService {

    override fun getWarpLimit(): Int {
        return configFile.getInt("warp_limit", 3)
    }

    override fun getTeleportTimer(): Int {
        return configFile.getInt("teleport_timer", 5)
    }

    override fun getTeleportCostType() {
        TODO("Not yet implemented")
    }

    override fun getTeleportCostAmount(): Int {
        return configFile.getInt("teleport_cost", 3)
    }
}