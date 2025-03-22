package dev.mizarc.waystonewarps.application.actions.discovery

import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.warps.Warp
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class GetFavouritedWarpAccess(private val discoveryRepository: DiscoveryRepository,
                          private val warpRepository: WarpRepository
) {
    fun execute(playerId: UUID): List<Warp> {
        val discoveries = discoveryRepository.getByPlayer(playerId)
        val warps = mutableListOf<Warp>()
        for (discovery in discoveries) {
            val warp = warpRepository.getById(discovery.warpId)
            if (warp != null && discovery.isFavourite) {
                warps.add(warp)
            }
        }
        return warps
    }
}