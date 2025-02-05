package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import java.util.*

class GetPlayersWithAccessToWarp(private val discoveryRepository: DiscoveryRepository) {
    fun execute(warpId: UUID): List<UUID> {
        val discoveries = discoveryRepository.getByWarp(warpId)
        return discoveries.map { it.playerId }
    }
}