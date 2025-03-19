package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType

interface ConfigService {
    fun getWarpLimit(): Int
    fun getTeleportTimer(): Int
    fun getTeleportCostType(): CostType
    fun getTeleportCostItem(): String
    fun getTeleportCostAmount(): Double
    fun getPlatformReplaceBlocks(): Set<String>
    fun getStructureBlocks(blockType: String): List<String>
}