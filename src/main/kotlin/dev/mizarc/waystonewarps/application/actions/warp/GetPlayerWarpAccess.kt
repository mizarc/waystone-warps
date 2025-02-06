package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import java.util.*

class GetPlayerWarpAccess(private val discoveryRepository: DiscoveryRepository) {
    fun execute(playerId: UUID): List<UUID> {
        val discoveries = discoveryRepository.getByPlayer(playerId)
        return discoveries.map { it.playerId }
    }
}