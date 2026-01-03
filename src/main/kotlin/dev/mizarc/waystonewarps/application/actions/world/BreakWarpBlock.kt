package dev.mizarc.waystonewarps.application.actions.world

import dev.mizarc.waystonewarps.application.results.BreakWarpResult
import dev.mizarc.waystonewarps.application.services.HologramService
import dev.mizarc.waystonewarps.application.services.StructureBuilderService
import dev.mizarc.waystonewarps.application.services.StructureParticleService
import dev.mizarc.waystonewarps.application.services.WarpEventPublisher
import dev.mizarc.waystonewarps.domain.discoveries.DiscoveryRepository
import dev.mizarc.waystonewarps.domain.positioning.Position3D
import dev.mizarc.waystonewarps.domain.warps.WarpRepository
import dev.mizarc.waystonewarps.domain.whitelist.WhitelistRepository
import java.util.UUID

class BreakWarpBlock(
    private val warpRepository: WarpRepository,
    private val structureBuilderService: StructureBuilderService,
    private val discoveryRepository: DiscoveryRepository,
    private val whitelistRepository: WhitelistRepository,
    private val structureParticleService: StructureParticleService,
    private val hologramService: HologramService,
    private val warpEventPublisher: WarpEventPublisher
) {
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
        
        // Remove all discoveries for this warp
        discoveryRepository.getByWarp(warp.id).forEach { discovery ->
            discoveryRepository.remove(discovery.warpId, discovery.playerId)
        }
        
        // Remove all whitelist entries for this warp
        whitelistRepository.removeByWarp(warp.id)
        
        structureBuilderService.revertStructure(warp)
        structureParticleService.removeParticles(warp)
        hologramService.removeHologram(warp)
        warpEventPublisher.warpDeleted(warp)
        return BreakWarpResult.Success(warp)
    }
}