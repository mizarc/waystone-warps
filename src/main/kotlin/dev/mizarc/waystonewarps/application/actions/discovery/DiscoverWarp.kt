package dev.mizarc.waystonewarps.application.actions.discovery

import dev.mizarc.waystonewarps.domain.discoveries.Discovery
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import java.time.Instant
import java.util.UUID

class DiscoverWarp(private val discoveryRepository: DiscoveryRepository) {
    fun execute(playerId: UUID, warpId: UUID): Boolean {
        if (discoveryRepository.getByWarpAndPlayer(warpId, playerId) != null) {
            return false
        }

        discoveryRepository.add(Discovery(warpId, playerId, Instant.now()))
        return true
    }
}