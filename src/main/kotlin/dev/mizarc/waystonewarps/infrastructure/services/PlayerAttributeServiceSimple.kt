package dev.mizarc.waystonewarps.infrastructure.services

import dev.mizarc.waystonewarps.application.services.ConfigService
import dev.mizarc.waystonewarps.application.services.PlayerAttributeService
import java.util.*

class PlayerAttributeServiceSimple(private val configService: ConfigService): PlayerAttributeService {
    override fun getWarpLimit(playerId: UUID): Int {
        return configService.getWarpLimit()
    }

    override fun getTeleportCost(playerId: UUID): Double {
        return configService.getTeleportCostAmount()
    }

    override fun getTeleportTimer(playerId: UUID): Int {
        return configService.getTeleportTimer()
    }
}