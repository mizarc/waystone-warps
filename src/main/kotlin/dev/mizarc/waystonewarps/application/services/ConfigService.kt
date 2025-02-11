package dev.mizarc.waystonewarps.application.services

interface ConfigService {
    fun getWarpLimit(): Int
    fun getTeleportTimer(): Int
    fun getTeleportCostType()
    fun getTeleportCostAmount(): Int
}