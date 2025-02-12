package dev.mizarc.waystonewarps.application.services

import dev.mizarc.waystonewarps.infrastructure.services.teleportation.CostType

interface ConfigService {
    fun getWarpLimit(): Int
    fun getTeleportTimer(): Int
    fun getTeleportCostType(): CostType
    fun getTeleportCostAmount(): Int
}