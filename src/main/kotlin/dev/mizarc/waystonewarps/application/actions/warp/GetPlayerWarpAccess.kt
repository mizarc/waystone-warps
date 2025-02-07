package dev.mizarc.waystonewarps.application.actions.warp

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.*

class GetPlayerWarpAccess(private val discoveryRepository: DiscoveryRepository,
                          private val warpRepository: WarpRepository) {
    fun execute(playerId: UUID): List<Warp> {
        val discoveries = discoveryRepository.getByPlayer(playerId)
        val warps = mutableListOf<Warp>()
        for (discovery in discoveries) {
            val warp = warpRepository.getById(discovery.warpId)
            if (warp != null) {
                warps.add(warp)
            }
        }
        return warps
    }
}