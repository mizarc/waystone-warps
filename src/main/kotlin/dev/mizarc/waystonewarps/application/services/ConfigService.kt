package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType

interface ConfigService {
    fun getPluginLanguage(): String
    fun getWarpLimit(): Int
    fun getTeleportTimer(): Int
    fun getTeleportCooldown(): Int
    fun isTeleportCostEnabled(): Boolean
    fun getTeleportCostType(): CostType
    fun getTeleportCostItem(): String
    fun getTeleportCostItemModel(): String
    fun getTeleportCostAmount(): Double
    fun isTeleportCostDistanceScaling(): Boolean
    fun getTeleportCostMin(): Int
    fun getTeleportCostMax(): Int
    fun getTeleportCostScaleDistance(): Double
    fun getPlatformReplaceBlocks(): Set<String>
    fun getAllSkinTypes(): List<String>
    fun getStructureBlocks(blockType: String): List<String>
    fun allowWarpsMenuViaCompass(): Boolean
    fun allowWarpsMenuViaWaystone(): Boolean
    fun hologramsEnabled(): Boolean
}
