package dev.mizarc.waystonewarps.application.actions.discovery

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import java.util.UUID

class ToggleFavouriteDiscovery(private val discoveryRepository: DiscoveryRepository) {
    fun execute(playerId: UUID, warpId: UUID): Boolean {
        val discovery = discoveryRepository.getByWarpAndPlayer(warpId, playerId) ?: return false
        discovery.isFavourite = !discovery.isFavourite
        discoveryRepository.update(discovery)
        return true
    }
}