package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import java.io.File

class ConfigServiceBukkit(private val configFile: FileConfiguration): ConfigService {

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

    override fun getPlatformReplaceBlocks(): Set<String> {
        return configFile.getStringList("platform_replace_blocks").toSet()
    }

    override fun getAllSkinTypes(): List<String> {
        return configFile.getConfigurationSection("waystone_skins")?.getKeys(false)?.toList() ?: emptyList()
    }

    override fun getStructureBlocks(blockType: String): List<String> {
        return configFile.getStringList("waystone_skins.$blockType")
    }

    override fun allowListMenuViaCompass(): Boolean {
        return configFile.getBoolean("list_menu_via_compass")
    }

    override fun allowListMenuViaWaystone(): Boolean {
        return configFile.getBoolean("list_menu_via_waystone")
    }
}