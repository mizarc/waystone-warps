package dev.mizarc.waystonewarps.application.actions.discovery

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import java.util.UUID

class RevokeDiscovery(private val discoveryRepository: DiscoveryRepository) {
    fun execute(playerId: UUID, warpId: UUID): Boolean {
        if (discoveryRepository.getByWarpAndPlayer(warpId, playerId) == null) {
            return false
        }

        discoveryRepository.remove(warpId, playerId)
        return true
    }
}