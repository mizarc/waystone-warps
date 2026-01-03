package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType

interface ConfigService {
    fun getPluginLanguage(): String
    fun getWarpLimit(): Int
    fun getTeleportTimer(): Int
    fun getTeleportCostType(): CostType
    fun getTeleportCostItem(): String
    fun getTeleportCostAmount(): Double
    fun getPlatformReplaceBlocks(): Set<String>
    fun getAllSkinTypes(): List<String>
    fun getStructureBlocks(blockType: String): List<String>
    fun allowWarpsMenuViaCompass(): Boolean
    fun allowWarpsMenuViaWaystone(): Boolean
    fun hologramsEnabled(): Boolean
}