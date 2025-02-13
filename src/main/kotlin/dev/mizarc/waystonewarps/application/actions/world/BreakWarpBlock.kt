package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.application.results.BreakWarpResult
import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import java.util.UUID

class BreakWarpBlock(private val warpRepository: WarpRepository,
                     private val structureBuilderService: StructureBuilderService,
                     private val discoveryRepository: DiscoveryRepository) {
    fun execute(position: Position3D, worldId: UUID): BreakWarpResult  {
        val warp = warpRepository.getByPosition(position, worldId) ?: return BreakWarpResult.WarpNotFound

        // Trigger break timer and decrement by 1
        warp.resetBreakCount()
        warp.breakCount -= 1

        // Warp still needs more breaks to fully break
        if (warp.breakCount > 0) {
            return BreakWarpResult.Breaking(warp.breakCount)
        }

        // Warp has been broken
        warpRepository.remove(warp.id)
        val discoveries = discoveryRepository.getByWarp(warp.id)
        for (discovery in discoveries) {
            discoveryRepository.remove(discovery.warpId, discovery.playerId)
        }
        structureBuilderService.despawnStructure(warp)
        return BreakWarpResult.Success(warp)
    }
}